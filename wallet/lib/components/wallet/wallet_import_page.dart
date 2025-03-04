import 'package:bac_web3/components/wallet/components/import_wallet_form.dart';
import 'package:bac_web3/components/wallet/model/wallet_setup.dart';
import 'package:flutter/material.dart';

class WalletImportPage extends StatefulWidget {
  const WalletImportPage(this.title, {super.key});

  final String title;

  @override
  State<WalletImportPage> createState() => _WalletImportPageState();
}

class _WalletImportPageState extends State<WalletImportPage> {
  bool loading = false;

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text(widget.title),
      ),
      body: ImportWalletForm(
        errors: [],
        onImport: !loading
            ? (type, value) async {
                switch (type) {
                  case WalletImportType.mnemonic:
                    // if (!await store.importFromMnemonic(value)) {
                    //   return;
                    // }
                    break;
                  case WalletImportType.privateKey:
                    // if (!await store.importFromPrivateKey(value)) {
                    //   return;
                    // }
                    break;
                  default:
                    break;
                }
                Navigator.of(context).popAndPushNamed('/');
              }
            : null,
      ),
    );
  }
}
