import 'package:bac_web3/components/wallet/components/copy_button.dart';
import 'package:flutter/material.dart';

class DisplayMnemonic extends StatelessWidget {
  final String mnemonic;

  const DisplayMnemonic({super.key, required this.mnemonic});

  @override
  Widget build(BuildContext context) {
    return Center(
      child: Container(
        constraints: const BoxConstraints(maxWidth: 420),
        margin: const EdgeInsets.all(25),
        child: SingleChildScrollView(
          child: Column(
            children: <Widget>[
              const Text(
                'Scrie undeva aceste cuvinte pentru a-ti putea recupera portofelul mai târziu',
                textAlign: TextAlign.center,
              ),
              Container(
                margin: const EdgeInsets.all(25),
                padding: const EdgeInsets.all(10),
                decoration: BoxDecoration(
                  border: Border.all(),
                  borderRadius: BorderRadius.circular(16),
                ),
                child: Text(mnemonic, textAlign: TextAlign.center),
              ),
              Row(
                mainAxisAlignment: MainAxisAlignment.spaceEvenly,
                children: <Widget>[
                  CopyButton(text: const Text('Copy'), value: mnemonic),
                ],
              )
            ],
          ),
        ),
      ),
    );
  }
}
