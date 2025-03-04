import 'package:bac_web3/components/auth/auth_service.dart';
import 'package:bac_web3/components/auth/login.dart';
import 'package:bac_web3/home_screen.dart';
import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';

final router = GoRouter(
  // redirect: (context, state) {
  //   final loggedIn = AuthService.instance.logininfo.isLoggedIn;
  //
  //   final isLoggingIn = state.path == '/login';
  //
  //   if (!loggedIn && !isLoggingIn) return '/login';
  //   if (loggedIn) return '/home';
  //
  //   return null;
  // },
  // refreshListenable: AuthService.instance.logininfo,
  debugLogDiagnostics: false,
  initialLocation: '/home',
      // AuthService.instance.logininfo.isLoggedIn ? '/home' : '/login',
  routes: [
    GoRoute(
      name: 'home',
      path: '/home',
      pageBuilder: (context, state) => MaterialPage(
        key: state.pageKey,
        child: const HomeScreen(),
      ),
    ),
    GoRoute(
      name: 'login',
      path: '/login',
      pageBuilder: (context, state) => MaterialPage(
        key: state.pageKey,
        child: const LoginScreen(),
      ),
    ),
  ],
  errorPageBuilder: (context, state) => MaterialPage(
    key: state.pageKey,
    child: Scaffold(
      body: Center(
        child: Text(state.error.toString()),
      ),
    ),
  ),
);
