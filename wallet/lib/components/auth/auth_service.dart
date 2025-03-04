import 'dart:async';
import 'dart:convert';
import 'dart:io';

import 'package:bac_web3/common/constants.dart';
import 'package:bac_web3/components/auth/model/auth_id_token.dart';
import 'package:bac_web3/components/auth/model/auth_user.dart';
import 'package:flutter/services.dart';
import 'package:flutter/widgets.dart';
import 'package:flutter_appauth/flutter_appauth.dart';
import 'package:flutter_secure_storage/flutter_secure_storage.dart';

typedef AsyncCallBackString = Future<String> Function();

class _LoginInfo extends ChangeNotifier {
  var _isLoggedIn = false;
  var _userRole = '';

  bool get isLoggedIn => _isLoggedIn;

  String get userRole => _userRole;

  set isLoggedIn(bool value) {
    _isLoggedIn = value;
    notifyListeners();
  }

  set userRole(String value) {
    _userRole = value;
    notifyListeners();
  }
}

class AuthService {
  static final AuthService instance = AuthService._internal();

  factory AuthService() {
    return instance;
  }

  AuthService._internal();

  final _loginInfo = _LoginInfo();

  String? accessToken;
  AuthIdToken? authIdToken;
  String? idTokenRaw;
  AuthUser? profile;

  get logininfo => _loginInfo;

  final appAuth = const FlutterAppAuth();
  final secureStorage = const FlutterSecureStorage();

  Future<String> init() async {
    return errorHandler(() async {
      final securedRefreshToken =
          await secureStorage.read(key: AUTH_REFRESH_TOKEN_KEY);

      if (securedRefreshToken == null) {
        return 'You need to login!';
      }

      try {
        final response = await appAuth.token(
          TokenRequest(
            AUTH_CLIENT_ID,
            AUTH_REDIRECT_URI,
            issuer: AUTH_ISSUER,
            refreshToken: securedRefreshToken,
            allowInsecureConnections: true,
          ),
        );

        return await _setLocalVariables(response);
      } catch (e) {
        if (e is PlatformException &&
            e.code == 'token_failed' &&
            e.details != null &&
            e.details.contains('invalid_grant')) {
          return 'Your session has expired. Please log in again.';
        } else {
          // Handle other potential exceptions
          return 'An unexpected error occurred. Please try again later.';
        }
      }
    });
  }

  bool isAuthResultValide(TokenResponse? response) {
    return response?.accessToken != null && response?.idToken != null;
  }

  Future<String> _setLocalVariables(TokenResponse? result) async {
    if (isAuthResultValide(result)) {
      accessToken = result!.accessToken!;
      idTokenRaw = result.idToken!;
      authIdToken = parseIdToken(idTokenRaw!);

      // profile = await getUserDetails(accessToken!);
      //print(profile!.email);

      if (result.refreshToken != null) {
        await secureStorage.write(
            key: AUTH_REFRESH_TOKEN_KEY, value: result.refreshToken);
      }

      _loginInfo.isLoggedIn = true;
      _loginInfo.userRole = authIdToken!.email;

      return 'SUCCESS';
    }

    return 'Passing Token went wrong';
  }

  Future<String> errorHandler(AsyncCallBackString callback) async {
    try {
      return callback();
    } on TimeoutException catch (e) {
      return e.message ?? 'Timeout Error!';
    } on FormatException catch (e) {
      return e.message;
    } on SocketException catch (e) {
      return e.message;
    } on PlatformException catch (e) {
      return e.message ?? 'Something is Wrong!';
    } catch (e) {
      return 'Unknown Error ${e.runtimeType}';
    }
  }

  Future<String> login() async {
    return errorHandler(() async {
      final authorizationTokenRequest = AuthorizationTokenRequest(
        AUTH_CLIENT_ID,
        AUTH_REDIRECT_URI,
        issuer: AUTH_ISSUER,
        // also request role
        scopes: ['openid', 'profile', 'email', 'roles'],
        promptValues: ['login'],
        allowInsecureConnections: true,
      );

      // Call Keycloak for authorize and exchange code
      final result =
          await appAuth.authorizeAndExchangeCode(authorizationTokenRequest);

      return _setLocalVariables(result);
    });
  }

  logout() async {
    await secureStorage.delete(key: AUTH_REFRESH_TOKEN_KEY);

    final request = EndSessionRequest(
      idTokenHint: idTokenRaw!,
      issuer: AUTH_ISSUER,
      postLogoutRedirectUrl: AUTH_REDIRECT_URI,
      allowInsecureConnections: true,
    );

    await appAuth.endSession(request);
    _loginInfo.isLoggedIn = false;
  }

  AuthIdToken parseIdToken(String idToken) {
    final parts = idToken.split(r'.');

    final Map<String, dynamic> json = jsonDecode(
        utf8.decode(base64Url.decode(base64Url.normalize(parts[1]))));

    return AuthIdToken.fromJson(json);
  }

  String userIdFromToken() {
    return authIdToken?.sub ?? '';
  }

// Future<AuthUser> getUserDetails(String accessToken) async {
//   final url = Uri.http(AUTH_DOMAIN,
//       '/auth/realms/${AUTH_REALMS}/protocol/openid-connect/userinfo');
//
//   final response = await http.get(
//     url,
//     headers: {
//       'Authorization': 'Bearer ${accessToken}',
//     },
//   );
//
//   print(response.statusCode);
//   print(response.body);
//
//   if (response.statusCode == 200) {
//     return AuthUser.fromJson(jsonDecode(response.body));
//   } else {
//     throw Exception('Failed to get user details!');
//   }
// }
}
