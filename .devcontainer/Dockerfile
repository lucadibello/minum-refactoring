FROM amazoncorretto:17-alpine3.19

# Install required packages
RUN apk add --no-cache bash curl tar gzip tmux git maven gradle eudev-dev openssh zip unzip

# Install additional build tools 
RUN apk add --no-cache build-base libtool autoconf automake cmake gettext gettext-dev gcc g++

#
# Install Neovim + related dependencies
#

# Install support for some languages (needed by neovim)
RUN apk add --no-cache python3 py3-pip nodejs npm rust cargo go perl

# Install additional tools needed by nvim
RUN apk add --no-cache ripgrep fzf lazygit

# Install Neovim from source
RUN git clone https://github.com/neovim/neovim /tmp/neovim && \
  cd /tmp/neovim && \
  git checkout stable && \
  make CMAKE_BUILD_TYPE=Release && \
  make install && \
  rm -rf /tmp/neovim

# Install tmux plugin manager
RUN git clone https://github.com/tmux-plugins/tpm ~/.tmux/plugins/tpm
RUN TMUX_PLUGIN_MANAGER_PATH=~/.tmux/plugins/tpm ~/.tmux/plugins/tpm/bin/install_plugins

# Generate new SSH key for the container (will be the repo deploy key)
RUN ssh-keygen -t ed25519 -f /root/.ssh/id_ed25519 -N ""

#
# Install context-specific tools
#

# Move to tmp directory to donwload files
WORKDIR /tmp

# 1) Install PMD (extensible cross-language static code analyzer)
RUN wget https://github.com/pmd/pmd/releases/download/pmd_releases%2F7.7.0/pmd-dist-7.7.0-bin.zip && \
  unzip pmd-dist-7.7.0-bin.zip && \
  mv pmd-bin-7.7.0 /opt/pmd && \
  rm pmd-dist-7.7.0-bin.zip

# 2) Install SonarQube Scanner (static code analysis tool)
RUN wget https://binaries.sonarsource.com/Distribution/sonar-scanner-cli/sonar-scanner-cli-6.2.1.4610.zip && \
  unzip sonar-scanner-cli-6.2.1.4610.zip && \
  mv sonar-scanner-6.2.1.4610 /opt/sonar-scanner && \
  rm sonar-scanner-cli-6.2.1.4610.zip

# Add binaries to PATH
ENV PATH=$PATH:/opt/pmd/bin:/opt/sonar-scanner/bin

# Set workdir
WORKDIR /workspace
