package com.revolsys.awt;

import java.awt.Color;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.LoggerFactory;

public class WebColors {
  private static final Map<Color, String> COLOR_NAMES = new LinkedHashMap<Color, String>();

  public static final Color AliceBlue = new Color(240, 248, 255);

  public static final Color AntiqueWhite = new Color(250, 235, 215);

  public static final Color Aqua = new Color(0, 255, 255);

  public static final Color Aquamarine = new Color(127, 255, 212);

  public static final Color Azure = new Color(240, 255, 255);

  public static final Color Beige = new Color(245, 245, 220);

  public static final Color Bisque = new Color(255, 228, 196);

  public static final Color Black = new Color(0, 0, 0);

  public static final Color BlanchedAlmond = new Color(255, 235, 205);

  public static final Color Blue = new Color(0, 0, 255);

  public static final Color BlueViolet = new Color(138, 43, 226);

  public static final Color Brown = new Color(165, 42, 42);

  public static final Color BurlyWood = new Color(222, 184, 135);

  public static final Color CadetBlue = new Color(95, 158, 160);

  public static final Color Chartreuse = new Color(127, 255, 0);

  public static final Color Chocolate = new Color(210, 105, 30);

  public static final Color Coral = new Color(255, 127, 80);

  public static final Color CornflowerBlue = new Color(100, 149, 237);

  public static final Color Cornsilk = new Color(255, 248, 220);

  public static final Color Crimson = new Color(220, 20, 60);

  public static final Color Cyan = new Color(0, 255, 255);

  public static final Color DarkBlue = new Color(0, 0, 139);

  public static final Color DarkCyan = new Color(0, 139, 139);

  public static final Color DarkGoldenRod = new Color(184, 134, 11);

  public static final Color DarkGray = new Color(169, 169, 169);

  public static final Color DarkGreen = new Color(0, 100, 0);

  public static final Color DarkKhaki = new Color(189, 183, 107);

  public static final Color DarkMagenta = new Color(139, 0, 139);

  public static final Color DarkOliveGreen = new Color(85, 107, 47);

  public static final Color DarkOrange = new Color(255, 140, 0);

  public static final Color DarkOrchid = new Color(153, 50, 204);

  public static final Color DarkRed = new Color(139, 0, 0);

  public static final Color DarkSalmon = new Color(233, 150, 122);

  public static final Color DarkSeaGreen = new Color(143, 188, 143);

  public static final Color DarkSlateBlue = new Color(72, 61, 139);

  public static final Color DarkSlateGray = new Color(47, 79, 79);

  public static final Color DarkTurquoise = new Color(0, 206, 209);

  public static final Color DarkViolet = new Color(148, 0, 211);

  public static final Color DeepPink = new Color(255, 20, 147);

  public static final Color DeepSkyBlue = new Color(0, 191, 255);

  public static final Color DimGray = new Color(105, 105, 105);

  public static final Color DimGrey = new Color(105, 105, 105);

  public static final Color DodgerBlue = new Color(30, 144, 255);

  public static final Color FireBrick = new Color(178, 34, 34);

  public static final Color FloralWhite = new Color(255, 250, 240);

  public static final Color ForestGreen = new Color(34, 139, 34);

  public static final Color Fuchsia = new Color(255, 0, 255);

  public static final Color Gainsboro = new Color(220, 220, 220);

  public static final Color GhostWhite = new Color(248, 248, 255);

  public static final Color Gold = new Color(255, 215, 0);

  public static final Color GoldenRod = new Color(218, 165, 32);

  public static final Color Gray = new Color(128, 128, 128);

  public static final Color Green = new Color(0, 128, 0);

  public static final Color GreenYellow = new Color(173, 255, 47);

  public static final Color HoneyDew = new Color(240, 255, 240);

  public static final Color HotPink = new Color(255, 105, 180);

  public static final Color IndianRed = new Color(205, 92, 92);

  public static final Color Indigo = new Color(75, 0, 130);

  public static final Color Ivory = new Color(255, 255, 240);

  public static final Color Khaki = new Color(240, 230, 140);

  public static final Color Lavender = new Color(230, 230, 250);

  public static final Color LavenderBlush = new Color(255, 240, 245);

  public static final Color LawnGreen = new Color(124, 252, 0);

  public static final Color LemonChiffon = new Color(255, 250, 205);

  public static final Color LightBlue = new Color(173, 216, 230);

  public static final Color LightCoral = new Color(240, 128, 128);

  public static final Color LightCyan = new Color(224, 255, 255);

  public static final Color LightGoldenRodYellow = new Color(250, 250, 210);

  public static final Color LightGray = new Color(211, 211, 211);

  public static final Color LightGreen = new Color(144, 238, 144);

  public static final Color LightPink = new Color(255, 182, 193);

  public static final Color LightSalmon = new Color(255, 160, 122);

  public static final Color LightSeaGreen = new Color(32, 178, 170);

  public static final Color LightSkyBlue = new Color(135, 206, 250);

  public static final Color LightSlateGray = new Color(119, 136, 153);

  public static final Color LightSteelBlue = new Color(176, 196, 222);

  public static final Color LightYellow = new Color(255, 255, 224);

  public static final Color Lime = new Color(0, 255, 0);

  public static final Color LimeGreen = new Color(50, 205, 50);

  public static final Color Linen = new Color(250, 240, 230);

  public static final Color Magenta = new Color(255, 0, 255);

  public static final Color Maroon = new Color(128, 0, 0);

  public static final Color MediumAquaMarine = new Color(102, 205, 170);

  public static final Color MediumBlue = new Color(0, 0, 205);

  public static final Color MediumOrchid = new Color(186, 85, 211);

  public static final Color MediumPurple = new Color(147, 112, 219);

  public static final Color MediumSeaGreen = new Color(60, 179, 113);

  public static final Color MediumSlateBlue = new Color(123, 104, 238);

  public static final Color MediumSpringGreen = new Color(0, 250, 154);

  public static final Color MediumTurquoise = new Color(72, 209, 204);

  public static final Color MediumVioletRed = new Color(199, 21, 133);

  public static final Color MidnightBlue = new Color(25, 25, 112);

  public static final Color MintCream = new Color(245, 255, 250);

  public static final Color MistyRose = new Color(255, 228, 225);

  public static final Color Moccasin = new Color(255, 228, 181);

  public static final Color NavajoWhite = new Color(255, 222, 173);

  public static final Color Navy = new Color(0, 0, 128);

  public static final Color OldLace = new Color(253, 245, 230);

  public static final Color Olive = new Color(128, 128, 0);

  public static final Color OliveDrab = new Color(107, 142, 35);

  public static final Color Orange = new Color(255, 165, 0);

  public static final Color OrangeRed = new Color(255, 69, 0);

  public static final Color Orchid = new Color(218, 112, 214);

  public static final Color PaleGoldenRod = new Color(238, 232, 170);

  public static final Color PaleGreen = new Color(152, 251, 152);

  public static final Color PaleTurquoise = new Color(175, 238, 238);

  public static final Color PaleVioletRed = new Color(219, 112, 147);

  public static final Color PapayaWhip = new Color(255, 239, 213);

  public static final Color PeachPuff = new Color(255, 218, 185);

  public static final Color Peru = new Color(205, 133, 63);

  public static final Color Pink = new Color(255, 192, 203);

  public static final Color Plum = new Color(221, 160, 221);

  public static final Color PowderBlue = new Color(176, 224, 230);

  public static final Color Purple = new Color(128, 0, 128);

  public static final Color Red = new Color(255, 0, 0);

  public static final Color RosyBrown = new Color(188, 143, 143);

  public static final Color RoyalBlue = new Color(65, 105, 225);

  public static final Color SaddleBrown = new Color(139, 69, 19);

  public static final Color Salmon = new Color(250, 128, 114);

  public static final Color SandyBrown = new Color(244, 164, 96);

  public static final Color SeaGreen = new Color(46, 139, 87);

  public static final Color SeaShell = new Color(255, 245, 238);

  public static final Color Sienna = new Color(160, 82, 45);

  public static final Color Silver = new Color(192, 192, 192);

  public static final Color SkyBlue = new Color(135, 206, 235);

  public static final Color SlateBlue = new Color(106, 90, 205);

  public static final Color SlateGray = new Color(112, 128, 144);

  public static final Color Snow = new Color(255, 250, 250);

  public static final Color SpringGreen = new Color(0, 255, 127);

  public static final Color SteelBlue = new Color(70, 130, 180);

  public static final Color Tan = new Color(210, 180, 140);

  public static final Color Teal = new Color(0, 128, 128);

  public static final Color Thistle = new Color(216, 191, 216);

  public static final Color Tomato = new Color(255, 99, 71);

  public static final Color Turquoise = new Color(64, 224, 208);

  public static final Color Violet = new Color(238, 130, 238);

  public static final Color Wheat = new Color(245, 222, 179);

  public static final Color White = new Color(255, 255, 255);

  public static final Color WhiteSmoke = new Color(245, 245, 245);

  public static final Color Yellow = new Color(255, 255, 0);

  public static final Color YellowGreen = new Color(154, 205, 50);
  static {
    for (final Field field : WebColors.class.getFields()) {
      final int modifiers = field.getModifiers();
      if (Modifier.isStatic(modifiers)) {
        final Class<?> fieldClass = field.getType();
        if (Color.class.isAssignableFrom(fieldClass)) {
          try {
            final Color color = (Color)field.get(null);
            COLOR_NAMES.put(color, field.getName());
          } catch (final Throwable e) {
            LoggerFactory.getLogger(WebColors.class)
              .error("Unable to get field value: " + field, e);
          }
        }
      }
    }
  }

  public static Color getColorWithOpacity(final Color color, final int opacity) {
    return new Color(color.getRed(), color.getGreen(), color.getBlue(), opacity);
  }

  public static String getName(final Color color) {
    final Color newColor = new Color(color.getRed(), color.getGreen(), color.getBlue());
    return COLOR_NAMES.get(newColor);
  }
}
