{ pkgs ? import <nixpkgs> {} }:

pkgs.mkShell {
  packages = [
    pkgs.jdk25
  ];

  LD_LIBRARY_PATH = pkgs.lib.makeLibraryPath [
    pkgs.vulkan-loader
    pkgs.wayland
    pkgs.libxkbcommon
    pkgs.libdecor
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

  shellHook = ''
    export SDL_VIDEODRIVER="wayland"
    echo "./gradlew build to build"
    echo "./gradlew runClient to launch"
  '';
}
