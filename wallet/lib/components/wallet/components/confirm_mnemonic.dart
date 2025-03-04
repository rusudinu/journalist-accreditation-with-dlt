import 'package:bac_web3/common/components/form/paper_form.dart';
import 'package:bac_web3/common/components/form/paper_input.dart';
import 'package:bac_web3/common/components/form/paper_validation_summary.dart';
import 'package:flutter/material.dart';

class ConfirmMnemonic extends StatelessWidget {
  const ConfirmMnemonic(
      {super.key, this.errors, this.onConfirm, this.onGenerateNew});

  final List<String>? errors;
  final Function(String)? onConfirm;
  final VoidCallback? onGenerateNew;

  @override
  Widget build(BuildContext context) {
    final mnemonicController = TextEditingController();
    return Center(
      child: Container(
        margin: const EdgeInsets.all(25),
        child: SingleChildScrollView(
          child: PaperForm(
            padding: 30,
            actionButtons: <Widget>[
              OutlinedButton(
                onPressed: onGenerateNew,
                child: const Text('Generate New'),
              ),
              ElevatedButton(
                onPressed: onConfirm != null
                    ? () => onConfirm!(mnemonicController.value.text)
                    : null,
                child: const Text('Confirm'),
              )
            ],
            children: <Widget>[
              if (errors != null) PaperValidationSummary(errors!),
              PaperInput(
                labelText: 'Confirm your seed',
                hintText: 'Please type your seed phrase again',
                maxLines: 2,
                controller: mnemonicController,
              ),
            ],
          ),
        ),
      ),
    );
  }
}
