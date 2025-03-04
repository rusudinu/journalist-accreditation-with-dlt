import 'package:flutter/material.dart';
import 'package:google_fonts/google_fonts.dart';

class CustomTheme {
  // #dab6c4, #7b886f, #b4dc7f, #feffa5, #ffa0ac
  static const Color primaryColor = Color.fromRGBO(180, 220, 127, 1);
  static const Color primaryDarkColor = Color.fromRGBO(123, 136, 111, 1);

  static final ThemeData lightTheme = ThemeData(
    useMaterial3: true,
    colorSchemeSeed: const Color(0xFF0b132b),
    visualDensity: VisualDensity.adaptivePlatformDensity,
    brightness: Brightness.light,
    highlightColor: primaryDarkColor,
    textTheme: TextTheme(
      labelLarge: GoogleFonts.lexendDeca(
          textStyle: const TextStyle(
              color: primaryDarkColor,
              letterSpacing: 0.5,
              fontSize: 14,
              fontWeight: FontWeight.w600)),
      labelMedium: GoogleFonts.lexendDeca(
          textStyle: const TextStyle(
              color: Colors.black,
              letterSpacing: 0.5,
              fontSize: 10,
              fontWeight: FontWeight.w500,
              decoration: TextDecoration.underline)),
      // small text (mainly on the login page)
      labelSmall: GoogleFonts.lexendDeca(
          textStyle: const TextStyle(
              color: Colors.black, letterSpacing: 0.5, fontSize: 10)),
      // the title of the pages
      headlineLarge: GoogleFonts.lexendDeca(
          textStyle: const TextStyle(
              color: primaryDarkColor,
              letterSpacing: 0.5,
              fontSize: 20,
              fontWeight: FontWeight.w600)),
      bodyMedium: GoogleFonts.lexendDeca(
          textStyle: const TextStyle(
              color: primaryDarkColor,
              fontSize: 16,
              fontWeight: FontWeight.w400)),
    ),
  );
}
