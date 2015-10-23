package com.revolsys.swing;

import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;

import com.revolsys.awt.WebColors;
import com.revolsys.collection.map.WeakCache;
import com.revolsys.util.OS;

public class Icons {
  public static final String BADGE_FOLDER = "/com/revolsys/famfamfam/silk/badges/";

  private static final Map<Icon, Reference<ImageIcon>> DISABLED_ICON_BY_ICON = new WeakCache<>();

  private static final Map<String, Reference<ImageIcon>> DISABLED_ICON_CACHE = new HashMap<>();

  private static final Map<Image, BufferedImage> DISABLED_IMAGE_CACHE = new WeakCache<>();

  private static final Map<String, Reference<ImageIcon>> ICON_CACHE = new HashMap<>();

  private static final Map<String, Reference<BufferedImage>> NAMED_DISABLED_IMAGE_CACHE = new HashMap<>();

  private static final Map<String, Reference<BufferedImage>> NAMED_IMAGE_CACHE = new HashMap<>();

  public static final String RESOURCE_FOLDER = "/com/revolsys/famfamfam/silk/icons/";

  public static void addIcon(final List<Icon> icons, Icon icon, final boolean enabled) {
    if (icon != null) {
      if (!enabled) {
        icon = Icons.getDisabledIcon(icon);
      }
      icons.add(icon);
    }
  }

  public static BufferedImage alpha(final Image image, final float percent) {
    BufferedImage bufferedImage;
    final int width = image.getWidth(null);
    final int height = image.getHeight(null);
    if (image instanceof BufferedImage) {
      bufferedImage = (BufferedImage)image;
    } else {
      bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
      final Graphics2D graphics = bufferedImage.createGraphics();
      graphics.drawImage(image, 0, 0, null);
      graphics.dispose();
    }
    final BufferedImage newImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

    final int[] avgLUT = new int[766];
    for (int i = 0; i < avgLUT.length; i++) {
      avgLUT[i] = i / 3;
    }

    for (int i = 0; i < width; i++) {
      for (int j = 0; j < height; j++) {
        final int rgb = bufferedImage.getRGB(i, j);
        final int alpha = rgb >> 24 & 0xff;
        final int red = rgb >> 16 & 0xff;
        final int green = rgb >> 8 & 0xff;
        final int blue = rgb & 0xff;
        final int newAlpha = (int)Math.ceil(alpha * percent);
        final int newRgb = WebColors.colorToRGB(newAlpha, red, green, blue);

        newImage.setRGB(i, j, newRgb);
      }
    }
    return newImage;
  }

  public static ImageIcon getAnimatedIcon(final String imageFileName) {
    final Class<?> clazz = Icons.class;
    final String resourceName = RESOURCE_FOLDER + imageFileName;
    URL url = clazz.getResource(resourceName);
    if (url == null) {
      final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
      url = classLoader.getResource("images/" + imageFileName);
      if (url == null) {
        url = classLoader.getResource("icons/" + imageFileName);
      }
    }
    return new ImageIcon(url);
  }

  public static BufferedImage getBadgeImage(final String imageName) {
    final String resourceName = BADGE_FOLDER + imageName + ".png";
    final InputStream in = Icons.class.getResourceAsStream(resourceName);
    return getImage(in);
  }

  public static Cursor getCursor(final String imageName) {
    return getCursor(imageName, 0, 0);

  }

  public static Cursor getCursor(final String imageName, final int delta) {
    return getCursor(imageName, delta, delta);
  }

  public static Cursor getCursor(final String imageName, final int dx, final int dy) {
    Image image = getImage(imageName);
    if (image == null) {
      return null;
    } else {
      final Toolkit toolkit = Toolkit.getDefaultToolkit();
      if (OS.isWindows()) {
        final BufferedImage newImage = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
        final Graphics graphics = newImage.getGraphics();
        graphics.drawImage(image, 0, 0, null);
        graphics.dispose();
        image = newImage;
      }
      return toolkit.createCustomCursor(image, new Point(dx, dy), imageName);
    }
  }

  public static Icon getDisabledIcon(final Icon icon) {
    if (icon == null) {
      return null;
    } else {
      ImageIcon disabledIcon = null;
      final Reference<ImageIcon> iconReference = DISABLED_ICON_BY_ICON.get(icon);
      if (iconReference != null) {
        disabledIcon = iconReference.get();
      }
      if (disabledIcon == null) {
        if (icon instanceof ImageIcon) {
          final ImageIcon imageIcon = (ImageIcon)icon;
          final Image image = imageIcon.getImage();
          final BufferedImage disabledImage = getDisabledImage(image);
          disabledIcon = new ImageIcon(disabledImage);
        } else {
          return icon;
        }
        DISABLED_ICON_BY_ICON.put(icon, new WeakReference<>(disabledIcon));
      }
      return disabledIcon;
    }
  }

  public static ImageIcon getDisabledIcon(final String imageName) {
    ImageIcon icon = null;
    Reference<ImageIcon> iconReference = DISABLED_ICON_CACHE.get(imageName);
    if (iconReference != null) {
      icon = iconReference.get();
    }
    if (icon == null) {
      final Image image = getDisabledImage(imageName);
      if (image == null) {
        return null;
      } else {
        icon = new ImageIcon(image);
        iconReference = new WeakReference<>(icon);
        DISABLED_ICON_CACHE.put(imageName, iconReference);
        DISABLED_ICON_BY_ICON.put(getIcon(imageName), iconReference);

      }
    }
    return icon;
  }

  public static BufferedImage getDisabledImage(final Image image) {
    BufferedImage disabledImage = DISABLED_IMAGE_CACHE.get(image);
    if (disabledImage == null) {
      disabledImage = alpha(image, 0.30f);
      DISABLED_IMAGE_CACHE.put(image, disabledImage);
    }
    return disabledImage;
  }

  public static Image getDisabledImage(final String imageName) {
    BufferedImage image = null;
    final Reference<BufferedImage> imageReference = NAMED_DISABLED_IMAGE_CACHE.get(imageName);
    if (imageReference != null) {
      image = imageReference.get();
    }
    if (image == null) {
      image = getImage(imageName);
      image = getDisabledImage(image);
      NAMED_DISABLED_IMAGE_CACHE.put(imageName, new WeakReference<>(image));
    }
    return image;
  }

  public static ImageIcon getIcon(final String imageName) {
    ImageIcon icon = null;
    final Reference<ImageIcon> iconReference = ICON_CACHE.get(imageName);
    if (iconReference != null) {
      icon = iconReference.get();
    }
    if (icon == null) {
      final Image image = getImage(imageName);
      if (image == null) {
        return null;
      } else {
        icon = new ImageIcon(image);
        ICON_CACHE.put(imageName, new WeakReference<>(icon));
      }
    }
    return icon;
  }

  public static Icon getIconWithBadge(final Icon baseIcon, final String badgeName) {
    final BufferedImage image = new BufferedImage(baseIcon.getIconWidth(), baseIcon.getIconHeight(),
      BufferedImage.TYPE_INT_ARGB);
    final Graphics graphics = image.createGraphics();
    baseIcon.paintIcon(null, graphics, 0, 0);
    final Image badgeImage = getBadgeImage(badgeName);
    graphics.drawImage(badgeImage, 0, 0, null);
    graphics.dispose();
    return new ImageIcon(image);
  }

  protected static BufferedImage getImage(final InputStream in) {
    if (in != null) {
      try {
        final BufferedImage image = ImageIO.read(in);
        final BufferedImage convertedImg = new BufferedImage(image.getWidth(), image.getHeight(),
          BufferedImage.TYPE_INT_ARGB);
        final Graphics graphics = convertedImg.getGraphics();
        graphics.drawImage(image, 0, 0, null);
        graphics.dispose();
        return convertedImg;
      } catch (final IOException e) {
      }
    }
    return null;

  }

  public static BufferedImage getImage(final String imageName) {
    BufferedImage image = null;
    final Reference<BufferedImage> imageReference = NAMED_IMAGE_CACHE.get(imageName);
    if (imageReference != null) {
      image = imageReference.get();
    }
    if (image == null) {
      final String fileExtension = "png";
      image = getImage(imageName, fileExtension);
      NAMED_IMAGE_CACHE.put(imageName, new WeakReference<>(image));
    }
    return image;
  }

  public static BufferedImage getImage(final String imageName, final String fileExtension) {
    BufferedImage image;
    final Class<?> clazz = Icons.class;
    final String resourceName = RESOURCE_FOLDER + imageName + "." + fileExtension;
    InputStream in = clazz.getResourceAsStream(resourceName);
    if (in == null) {
      in = Thread.currentThread()
        .getContextClassLoader()
        .getResourceAsStream("images/" + imageName + "." + fileExtension);
      if (in == null) {
        in = Thread.currentThread()
          .getContextClassLoader()
          .getResourceAsStream("icons/" + imageName + "." + fileExtension);
      }
    }
    image = getImage(in);
    return image;
  }

  public static BufferedImage grayscale(final BufferedImage original) {
    final int width = original.getWidth();
    final int height = original.getHeight();
    final int type = original.getType();
    final BufferedImage newImage = new BufferedImage(width, height, type);

    final int[] avgLUT = new int[766];
    for (int i = 0; i < avgLUT.length; i++) {
      avgLUT[i] = i / 3;
    }

    for (int i = 0; i < width; i++) {
      for (int j = 0; j < height; j++) {
        final int rgb = original.getRGB(i, j);
        final int alpha = rgb >> 24 & 0xff;
        final int red = rgb >> 16 & 0xff;
        final int green = rgb >> 8 & 0xff;
        final int blue = rgb & 0xff;

        int newRgb = red + green + blue;
        newRgb = avgLUT[newRgb];
        newRgb = WebColors.colorToRGB(alpha, newRgb, newRgb, newRgb);

        newImage.setRGB(i, j, newRgb);
      }
    }
    return newImage;
  }

  public static Icon merge(final List<Icon> icons, final int space) {
    int maxWidth = 0;
    int maxHeight = 0;
    int i = 0;
    for (final Icon icon : icons) {
      if (icon != null) {
        maxWidth += icon.getIconWidth();
        maxHeight = Math.max(maxHeight, icon.getIconHeight());
        i++;
      }
    }
    maxWidth += (i - 1) * space;

    if (maxWidth == 0) {
      return null;
    }
    if (maxHeight == 0) {
      return null;
    }

    final BufferedImage newImage = new BufferedImage(maxWidth, maxHeight,
      BufferedImage.TYPE_INT_ARGB);

    final Graphics g = newImage.createGraphics();
    int x = 0;
    for (final Icon icon : icons) {
      if (icon != null) {
        final Image image = ((ImageIcon)icon).getImage();
        final int iconWidth = icon.getIconWidth();
        final int iconHeight = icon.getIconHeight();
        g.drawImage(image, x, 0, iconWidth, iconHeight, null);
        x += iconWidth;
        x += space;
      }
    }

    return new ImageIcon(newImage);
  }

}
