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
        pkgs.libGL
        pkgs.glfw
        pkgs.openal
        pkgs.libpulseaudio
        pkgs.udev
        pkgs.xorg.libX11
        pkgs.xorg.libXcursor
        pkgs.xorg.libXrandr
        pkgs.xorg.libXi
        pkgs.xorg.libXext
      ];
    };
  };
}
