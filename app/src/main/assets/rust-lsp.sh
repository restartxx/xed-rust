set -e

source "$LOCAL/bin/utils"

info 'Preparing...'
apt update && apt upgrade -y

RUST_ANALYZER_VERSION="$1"
INSTALL_DIR="$HOME/.lsp/rust"

get_arch() {
  case "$(uname -m)" in
    x86_64)
      echo "x86_64-unknown-linux-gnu"
      ;;
    aarch64 | arm64)
      echo "aarch64-unknown-linux-gnu"
      ;;
    *)
      error "Unsupported architecture: $(uname -m)"
      exit 1
      ;;
  esac
}

install() {
  info 'Installing rust-analyzer language server...'

  ARCH=$(get_arch)
  URL="https://github.com/rust-lang/rust-analyzer/releases/download/${RUST_ANALYZER_VERSION}/rust-analyzer-${ARCH}.gz"

  mkdir -p "$INSTALL_DIR"
  cd "$INSTALL_DIR"

  apt install -y curl ca-certificates gzip

  curl -L -o rust-analyzer.gz "$URL"

  info "Extracting..."
  gunzip -f rust-analyzer.gz

  chmod +x rust-analyzer

  echo "$RUST_ANALYZER_VERSION" > version.txt

  info 'rust-analyzer installed successfully.'
  exit 0
}

uninstall() {
  info 'Uninstalling rust-analyzer language server...'

  rm -rf "$INSTALL_DIR"

  info 'rust-analyzer uninstalled successfully.'
  exit 0
}

update() {
  info 'Updating rust-analyzer language server...'

  ARCH=$(get_arch)
  URL="https://github.com/rust-lang/rust-analyzer/releases/download/${RUST_ANALYZER_VERSION}/rust-analyzer-${ARCH}.gz"

  cd "$INSTALL_DIR"

  rm -f rust-analyzer rust-analyzer.gz

  curl -L -o rust-analyzer.gz "$URL"

  info "Extracting..."
  gunzip -f rust-analyzer.gz

  chmod +x rust-analyzer

  echo "$RUST_ANALYZER_VERSION" > version.txt

  info 'rust-analyzer updated successfully.'
  exit 0
}

case "$1" in
  --uninstall) uninstall;;
  --update) update;;
  *) install;;
esac