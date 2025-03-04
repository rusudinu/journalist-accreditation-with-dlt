enum WalletSetupMethod { fromMnemonic, fromPrivateKey, create }

enum WalletCreateSteps { display, confirm }

enum WalletImportType { mnemonic, privateKey }

class WalletSetup {
  WalletCreateSteps step;
  WalletSetupMethod method;
  bool loading;
  List<String> errors;

  WalletSetup({
    WalletCreateSteps? step,
    WalletSetupMethod? method,
    bool? loading,
    List<String>? errors,
  })  : step = step ?? WalletCreateSteps.display,
        method = method ?? WalletSetupMethod.create,
        loading = loading ?? false,
        errors = errors ?? [];
}
