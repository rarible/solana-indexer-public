FROM arm64v8/node:lts-bullseye as build

RUN apt-get update \
    && apt-get install -y bzip2 \
    libssl-dev libudev-dev clang \
    wget curl gcc pkg-config make \
    libpixman-1-dev libcairo2-dev libpango1.0-dev libjpeg62-turbo-dev libgif-dev \
    build-essential python-dev libpcap-dev libssl-dev

# explicitly set user/group IDs
RUN set -eux; \
        groupadd -r solana --gid=999; \
        useradd -r -g solana --uid=999 --home-dir=/home/solana --shell=/bin/bash solana; \
        mkdir -p /home/solana; \
        chown -R solana:solana /home/solana
USER solana
WORKDIR /home/solana

ENV SOLANA_VERSION=1.9.14
RUN wget -O solana-$SOLANA_VERSION.tar.gz https://github.com/solana-labs/solana/archive/refs/tags/v$SOLANA_VERSION.tar.gz
RUN curl https://sh.rustup.rs -sSf | sh -s -- -y

RUN . ~/.cargo/env && echo $PATH
ENV PATH="~/.cargo/bin:${PATH}"

RUN cd ~ && tar -xvf solana-$SOLANA_VERSION.tar.gz
RUN ./solana-$SOLANA_VERSION/scripts/cargo-install-all.sh .

RUN wget -O metaplex.zip https://github.com/rarible/metaplex/archive/refs/heads/master.zip
RUN unzip metaplex.zip

RUN npm install ts-node
RUN cd /home/solana/metaplex-master/js && yarn install

RUN ~/.cargo/bin/cargo install --git https://github.com/project-serum/anchor --tag v0.22.1 anchor-cli --locked

FROM arm64v8/node:lts-bullseye-slim as target
RUN set -eux; \
        groupadd -r solana --gid=999; \
        useradd -r -g solana --uid=999 --home-dir=/home/solana --shell=/bin/bash solana; \
        mkdir -p /home/solana; \
        chown -R solana:solana /home/solana
RUN apt-get update
RUN apt-get install -y bzip2
RUN npm install -g ts-node

USER solana
WORKDIR /home/solana

COPY --chown=solana:solana id.json /home/solana/.config/solana/
COPY --chown=solana:solana mpl_token_metadata.so /home/solana/
COPY --chown=solana:solana mpl_auction_house.so /home/solana/
COPY --chown=solana:solana auction_house.json /home/solana/
COPY --from=build /home/solana/bin /usr/bin
COPY --from=build /home/solana/metaplex-master /home/solana/metaplex
COPY --from=build /home/solana/.cargo/bin/anchor /usr/bin

RUN solana config set --url localhost
RUN anchor init tmp