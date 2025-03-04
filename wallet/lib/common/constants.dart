import 'package:logging/logging.dart';

const AUTH_DOMAIN = "auth.codingshadows.com";
const AUTH_CLIENT_ID = "bac3-authz-client";
const AUTH_ISSUER = "https://$AUTH_DOMAIN/realms/$AUTH_REALMS";
const BUNDLE_IDENTIFIER = "com.example.bacweb3";
const AUTH_REDIRECT_URI = "$BUNDLE_IDENTIFIER://login-callback";
const AUTH_REFRESH_TOKEN_KEY = 'refresh-token';
const AUTH_REALMS = "bac3";
// const BACKEND_URL = "10.0.2.2:8000";
const BACKEND_URL =
    "https://92b9-2a02-2f00-3002-4100-56f-cdeb-4915-adca.ngrok-free.app";
const BACKEND_API_PREFIX = "/api/v1";
const RSA_KEY_LENGTH = 2048;
const LOGGING_LEVEL = Level.ALL;
