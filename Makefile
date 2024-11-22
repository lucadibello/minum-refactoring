MAVEN = mvn
DIR = ./minum/

devcontainer: sonarqube
	@devcontainer build --workspace-folder .
	@devcontainer up --workspace-folder .
	@devcontainer exec --workspace-folder . tmux new-session -A -s dev

sonarqube:
	@docker start sonarqube-server || docker run --name sonarqube-server -p 9000:9000 sonarqube:lts-community &

build:
	cd $(DIR) && $(MAVEN) clean compile

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
.PHONY: devcontainer build sonarqube
