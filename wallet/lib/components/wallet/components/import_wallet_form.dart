import 'package:bac_web3/common/components/form/paper_form.dart';
import 'package:bac_web3/common/components/form/paper_input.dart';
import 'package:bac_web3/common/components/form/paper_radio.dart';
import 'package:bac_web3/common/components/form/paper_validation_summary.dart';
import 'package:bac_web3/components/wallet/model/wallet_setup.dart';
import 'package:flutter/material.dart';

class ImportWalletForm extends StatelessWidget {
  const ImportWalletForm({super.key, this.onImport, this.errors});

  final Function(WalletImportType type, String value)? onImport;
  final List<String>? errors;

  @override
  Widget build(BuildContext context) {
    const importType = WalletImportType.mnemonic;
    final inputController = TextEditingController();

    return Center(
      child: Container(
        margin: const EdgeInsets.all(25),
        child: SingleChildScrollView(
          child: PaperForm(
            padding: 30,
            actionButtons: <Widget>[
              // ElevatedButton(
              //   onPressed: onImport != null
              //       ? () =>
              //           onImport!(importType.value, inputController.value.text)
              //       : null,
              //   child: const Text('Import'),
              // )
            ],
            children: <Widget>[
              // Row(
              //   children: <Widget>[
              //     PaperRadio(
              //       'Seed',
              //       groupValue: importType.value,
              //       value: WalletImportType.mnemonic,
              //       onChanged: (value) =>
              //           importType.value = value as WalletImportType,
              //     ),
              //     PaperRadio(
              //       'Private Key',
              //       groupValue: importType.value,
              //       value: WalletImportType.privateKey,
              //       onChanged: (value) =>
              //           importType.value = value as WalletImportType,
              //     ),
              //   ],
              // ),
              // Column(
              //   children: <Widget>[
              //     Visibility(
              //         visible: importType.value == WalletImportType.privateKey,
              //         child: fieldForm(
              //             label: 'Private Key',
              //             hintText: 'Type your private key',
              //             controller: inputController)),
              //     Visibility(
              //         visible: importType.value == WalletImportType.mnemonic,
              //         child: fieldForm(
              //             label: 'Seed phrase',
              //             hintText: 'Type your seed phrase',
              //             controller: inputController)),
              //   ],
              // ),
            ],
          ),
        ),
      ),
    );
  }

  Widget fieldForm({
    required String label,
    required String hintText,
    required TextEditingController controller,
  }) {
    return Column(
      children: <Widget>[
        if (errors != null) PaperValidationSummary(errors!),
        PaperInput(
          labelText: label,
          hintText: hintText,
          maxLines: 3,
          controller: controller,
        ),
      ],
    );
  }
}
