import 'package:flutter/material.dart';

class Section extends StatelessWidget {
  final String sectionTitle;
  final Widget sectionContent;

  const Section(
      {required this.sectionTitle, required this.sectionContent, super.key});

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 10),
      child: Card(
          child: Padding(
        padding: const EdgeInsets.all(10),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Padding(
              padding: const EdgeInsets.only(left: 10, top: 5),
              child: Text(sectionTitle),
            ),
            sectionContent
          ],
        ),
      )),
    );
  }
}
