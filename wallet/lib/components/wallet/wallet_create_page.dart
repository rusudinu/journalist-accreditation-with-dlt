import 'package:bac_web3/common/app_data_bloc.dart';
import 'package:bac_web3/components/wallet/components/display_mnemonic.dart';
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

class WalletCreatePage extends StatefulWidget {
  const WalletCreatePage({super.key});

  @override
  State<WalletCreatePage> createState() => _WalletCreatePageState();
}

class _WalletCreatePageState extends State<WalletCreatePage> {
  bool displayMnemonic = true;
  String mnemonic = '';

  @override
  void initState() {
    super.initState();
    generateMnemonic();
  }

  void generateMnemonic() {
    setState(() {
      mnemonic = context.read<AppDataBloc>().generateMnemonic();
    });

    Future.delayed(const Duration(seconds: 1), () async {
      String privateKey =
          await context.read<AppDataBloc>().getPrivateKey(mnemonic);
      print(privateKey);
      print(
          context.read<AppDataBloc>().getPublicAddressWithChecksum(privateKey));
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Create wallet'),
      ),
      body: DisplayMnemonic(
        mnemonic: mnemonic,
      ),
    );
  }
}
