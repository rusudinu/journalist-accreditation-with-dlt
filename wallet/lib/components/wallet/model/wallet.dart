import 'package:json_annotation/json_annotation.dart';

part 'wallet.g.dart';

@JsonSerializable()
class Wallet {
  BigInt tokenBalance;
  BigInt ethBalance;
  List<dynamic> errors;
  bool loading;

  Wallet({
    BigInt? tokenBalance,
    BigInt? ethBalance,
    List<dynamic>? errors,
    bool? loading,
  })
      : tokenBalance = tokenBalance ?? BigInt.from(0),
        ethBalance = ethBalance ?? BigInt.from(0),
        errors = errors ?? [],
        loading = loading ?? false;
}
