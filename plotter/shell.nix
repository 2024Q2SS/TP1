{ pkgs ? import <nixpkgs> {} }:

pkgs.mkShell {
  buildInputs = [
    pkgs.python39Packages.numpy
    pkgs.python39Packages.pandas
    pkgs.python39Packages.matplotlib
    pkgs.python39
  ];
}
