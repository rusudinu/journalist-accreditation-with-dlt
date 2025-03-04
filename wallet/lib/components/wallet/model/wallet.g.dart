// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'wallet.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

Wallet _$WalletFromJson(Map<String, dynamic> json) => Wallet(
      tokenBalance: json['tokenBalance'] == null
          ? null
          : BigInt.parse(json['tokenBalance'] as String),
      ethBalance: json['ethBalance'] == null
          ? null
          : BigInt.parse(json['ethBalance'] as String),
      errors: json['errors'] as List<dynamic>?,
      loading: json['loading'] as bool?,
    );

Map<String, dynamic> _$WalletToJson(Wallet instance) => <String, dynamic>{
      'tokenBalance': instance.tokenBalance.toString(),
      'ethBalance': instance.ethBalance.toString(),
      'errors': instance.errors,
      'loading': instance.loading,
    };
