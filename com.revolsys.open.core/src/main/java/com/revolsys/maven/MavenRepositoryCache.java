package com.revolsys.maven;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.revolsys.spring.resource.FileSystemResource;
import org.springframework.core.io.Resource;

import com.revolsys.spring.resource.SpringUtil;
import com.revolsys.util.HexConverter;
import com.revolsys.util.Property;

public class MavenRepositoryCache extends MavenRepository {
  private static final Logger LOG = LoggerFactory.getLogger(MavenRepository.class);

  private List<MavenRepository> repositories = new ArrayList<MavenRepository>();

  public MavenRepositoryCache() {
  }

  public MavenRepositoryCache(final Resource root) {
    super(root);
  }

  public MavenRepositoryCache(final Resource root, final String... repositoryUrls) {
    super(root);
    for (String repository : repositoryUrls) {
      if (!repository.endsWith("/")) {
        repository += '/';
      }
      final Resource resource = SpringUtil.getResource(repository);
      this.repositories.add(new MavenRepository(resource));
    }
  }

  public MavenRepositoryCache(final String... repositoryUrls) {
    this(null, repositoryUrls);
  }

  public boolean copyRepositoryResource(final Resource resource, final MavenRepository repository,
    final String path, final String sha1Digest) {
    final Resource repositoryResource = SpringUtil.getResource(repository.getRoot(), path);
    if (repositoryResource.exists()) {
      try {
        if (Property.hasValue(sha1Digest)) {
          final InputStream in = SpringUtil.getInputStream(repositoryResource);
          final DigestInputStream digestIn = new DigestInputStream(in,
            MessageDigest.getInstance("SHA-1"));
          SpringUtil.copy(digestIn, resource);
          final MessageDigest messageDigest = digestIn.getMessageDigest();
          final byte[] digest = messageDigest.digest();
          final String fileDigest = HexConverter.toHex(digest);
          if (!sha1Digest.equals(fileDigest)) {
            LoggerFactory.getLogger(getClass()).error(
              ".sha1 digest is different for: " + repositoryResource);
            SpringUtil.delete(resource);
            return false;
          }
        } else {
          SpringUtil.copy(repositoryResource, resource);
        }
        return true;
      } catch (final Exception e) {
        SpringUtil.delete(resource);
        LOG.warn("Unable to download " + repositoryResource, e);
      }
    }
    return false;
  }

  public List<MavenRepository> getRepositories() {
    return this.repositories;
  }

  @Override
  protected Resource handleMissingResource(final Resource resource, final String groupId,
    final String artifactId, final String type, final String classifier, final String version,
    final String algorithm) {
    if (version.endsWith("-SNAPSHOT")) {
      final TreeMap<String, MavenRepository> versionsByRepository = new TreeMap<String, MavenRepository>();

      for (final MavenRepository repository : this.repositories) {
        final Map<String, Object> mavenMetadata = repository.getMavenMetadata(groupId, artifactId,
          version);
        final String snapshotVersion = getSnapshotVersion(mavenMetadata);
        if (snapshotVersion != null) {
          final String timestampVersion = version.replaceAll("SNAPSHOT$", snapshotVersion);
          versionsByRepository.put(timestampVersion, repository);
        }
      }
      if (!versionsByRepository.isEmpty()) {
        final Entry<String, MavenRepository> entry = versionsByRepository.lastEntry();
        final String timestampVersion = entry.getKey();

        final String path = getPath(groupId, artifactId, version, type, classifier,
          timestampVersion, algorithm);
        final Resource cachedResource = SpringUtil.getResource(getRoot(), path);
        if (cachedResource.exists()) {
          return cachedResource;
        } else {

          final MavenRepository repository = entry.getValue();
          final String sha1Digest = repository.getSha1(groupId, artifactId, type, classifier,
            version, algorithm);

          if (copyRepositoryResource(cachedResource, repository, path, sha1Digest)) {
            return cachedResource;
          }
        }

      }
    }
    final String path = getPath(groupId, artifactId, version, type, classifier, version, algorithm);
    for (final MavenRepository repository : this.repositories) {
      final String sha1Digest = repository.getSha1(groupId, artifactId, type, classifier, version,
        algorithm);
      if (copyRepositoryResource(resource, repository, path, sha1Digest)) {
        return resource;
      }
    }
    return resource;
  }

  public void setRepositories(final List<MavenRepository> repositories) {
    this.repositories = repositories;
  }

  public void setRepositoryLocations(final List<Resource> repositoryLocations) {
    for (final Resource resource : repositoryLocations) {
      this.repositories.add(new MavenRepository(resource));
    }
  }

  @Override
  public void setRoot(final Resource root) {
    if (root != null) {
      try {
        final File file = root.getFile();
        if (!file.exists()) {
          if (!file.mkdirs()) {
            throw new IllegalArgumentException("Cannot create maven cache directory " + file);
          }
        } else if (!file.isDirectory()) {
          throw new IllegalArgumentException("Maven cache is not a directory directory " + file);
        }
        final FileSystemResource fileResource = new FileSystemResource(file);
        super.setRoot(fileResource);
      } catch (final IOException e) {
        throw new IllegalArgumentException("Maven cache must resolve to a local directory " + root);
      }
    }
  }
}
