{
  inputs.nixpkgs.url = "github:nixos/nixpkgs/nixos-unstable";
  outputs = { self, nixpkgs }:
  let
    system = "x86_64-linux";
    pkgs = import nixpkgs { inherit system; };
  in {
    devShells.${system}.default = pkgs.mkShell {
      buildInputs = [ pkgs.jdk25 ];
      LD_LIBRARY_PATH = pkgs.lib.makeLibraryPath [
        pkgs.vulkan-loader
        pkgs.libGL
        pkgs.openal
        pkgs.libpulseaudio
        pkgs.udev
        pkgs.libX11
        pkgs.libXcursor
        pkgs.libXrandr
        pkgs.libXi
        pkgs.libXext
      ];
    };
  };
}
