GRADLE = ./gradlew
DIR = ./resilience4j

devcontainer: sonarqube
	@devcontainer build --workspace-folder .
	@devcontainer up --workspace-folder .

sonarqube:
	@docker start sonarqube-server || docker run --name sonarqube-server -p 9000:9000 sonarqube:lts-community &

attach: devcontainer
	@devcontainer exec --workspace-folder . tmux new-session -A -s dev

build:
	cd $(DIR) && $(GRADLE) build -x test

export:
	@cd report && pandoc assignment_report.tex -o ../README.md \
    --from=latex \
    --to=gfm \
    --wrap=none \
    --standalone \
    --highlight-style=pygments \
    --toc \
    --number-sections \
    --filter pandoc-crossref \
    --citeproc


# Phony targets
.PHONY: devcontainer build sonarqube attach
