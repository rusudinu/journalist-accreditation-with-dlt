import 'package:flutter/cupertino.dart';

class PaperValidationSummary extends StatelessWidget {
  const PaperValidationSummary(this.errors, {super.key});

  final List<String> errors;

  @override
  Widget build(BuildContext context) {
    return Column(
      children: errors.map((error) => Text(error)).toList(),
    );
  }
}
