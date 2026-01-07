# To activate this environment, run:
#   nix-shell shell.nix
let
  nixpkgs = import <nixpkgs> {};
in
  with nixpkgs;
  stdenv.mkDerivation {
    name = "sitegen-env";
    buildInputs = [
      jdk21_headless
      gradle
    ];
  }