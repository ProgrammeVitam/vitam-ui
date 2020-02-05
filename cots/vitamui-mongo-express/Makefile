#
# Makefile for packaging COTS component of vitamui solution
#

# Args parsing with default values
NAME                := mongo-express
VERSION             := 3.4.9
DEPENDENCIES        := nodejs,npm

# Src configuration:
SRC_DIR             := $(abspath  ./src)
BIN                 =  $(abspath $(SRC_DIR)/node_modules/mongo-express/app.js)

# Staging root configuration
DESTDIR             := $(abspath ./package-stage/)
STAGING_ROOT        := $(abspath $(DESTDIR)/)
FPM                 := /usr/local/bin/fpm
USER                := vitamuidb
GROUP               := vitamui
PACKAGING_MAKEFILE  := ../../tools/packaging/Makefile

# Package metas
COPYRIGHT           = VitamUI Copyright 2018 VITAM_UI
COMMIT_ID           = $(shell  git rev-parse --short HEAD)
BRANCHE             = $(shell  git rev-parse --abbrev-ref HEAD)
DESCRIPTION         = $(NAME) package for vitamui solution. Branch: $(BRANCHE), commit: $(COMMIT_ID)

# Install params
SYSTEMD_DIR         := /usr/lib/systemd/system/
VITAMUI_DIRS           =  app bin lib script conf log data tmp run defaults
PACKAGING_DIR       := $(dir $(lastword $(MAKEFILE_LIST)))
TEMPLATE_TMP_DIR    := $(abspath ./package-templates/)
TEMPLATE_SRC        = $(wildcard $(PACKAGING_DIR)/templates/*)
RPM_OUTPUT_PATH     := vitamui-$(NAME)-$(VERSION).rpm

# fpm packaging default args
FPM_PACK_ARGS               = -f -d "$(DEPENDENCIES)" --description "$(DESCRIPTION)"
FPM_PACK_META               = --license "$(COPYRIGHT)" -n "vitamui-$(NAME)" -v $(VERSION)
FPM_PACK_INSTALL_SCRIPTS    = --before-install "$(TEMPLATE_TMP_DIR)/before-install.sh" --after-install "$(TEMPLATE_TMP_DIR)/after-install.sh"
FPM_PACK_REMOVE_SCRIPTS     = --before-remove "$(TEMPLATE_TMP_DIR)/before-remove.sh" --after-remove "$(TEMPLATE_TMP_DIR)/after-remove.sh"

#
# Imports from packaging Makefiles
#

clean:
	$(MAKE) -f $(PACKAGING_MAKEFILE) NAME=$(NAME) VERSION=$(VERSION) RPM_OUTPUT_PATH=$(RPM_OUTPUT_PATH) $@

stage:
	$(MAKE) -f $(PACKAGING_MAKEFILE) NAME=$(NAME) VERSION=$(VERSION) STAGING_ROOT=$(STAGING_ROOT) $@

vitamui-dirs:
	$(MAKE) -f $(PACKAGING_MAKEFILE) NAME=$(NAME) VERSION=$(VERSION) $@

reset: clean
	@rm -Rf $(SRC_DIR)


#
# COTS src fetching:
#

# Prepare src dir
$(SRC_DIR):
	@mkdir -p $(SRC_DIR)

# Install node module

# Extract RPM to get pre  staged install files from logstash
$(BIN): $(SRC_DIR)
	@cp ../package.json $(SRC_DIR);  cd $(SRC_DIR) ; npm install mongo-express

source: $(BIN)

#
# COTS stage install
#

template-files:
	# Applying packaging/Makefile first:
	@$(MAKE) -f $(PACKAGING_MAKEFILE) NAME=$(NAME) VERSION=$(VERSION) $@

	# Override it with current directory template dir
	@mkdir -p $(TEMPLATE_TMP_DIR)
	@for fic in  $(TEMPLATE_SRC) ; \
	do \
		sed  \
			-e "s/__NAME__/$(NAME)/g" \
			-e "s/__JAR_FILE__/$(JAR_FILE)/g" \
			-e "s/__VERSION__/$(VERSION)/g" \
			-e "s/__USER__/$(USER)/g" \
			-e "s/__GROUP__/$(GROUP)/g" \
			-e "s/__ARGS__/$(ARGS)/g" \
			$$fic >  $(TEMPLATE_TMP_DIR)/`basename $$fic`; \
	done

install:  stage vitamui-dirs template-files source
	@mkdir   -p -m 750 $(abspath $(STAGING_ROOT)/$(SYSTEMD_DIR)/)
	@install -m 644 $(TEMPLATE_TMP_DIR)/vitamui-mongo-express.service $(abspath $(STAGING_ROOT)/$(SYSTEMD_DIR)/vitamui-mongo-express.service)
	# Now install the all node_modules in /vitamui/app/$(NAME)
	@cp -R -t $(abspath $(STAGING_ROOT)/vitamui/app/$(NAME)/) $(SRC_DIR)/*
	# Install config environnement vars:
	install -m 644 $(TEMPLATE_TMP_DIR)/vitamui-mongo-express-default-env.sh $(abspath $(STAGING_ROOT)/vitamui/defaults/$(NAME)/env.sh)

rpm: install
	@$(FPM) -s dir $(FPM_PACK_ARGS) $(FPM_PACK_META) $(FPM_PACK_INSTALL_SCRIPTS) $(FPM_PACK_REMOVE_SCRIPTS) -t rpm -p $(RPM_OUTPUT_PATH)  "$(STAGING_ROOT)/=/"

#
# COTS Packaging
#

.PHONY: clean stage vitamui-dirs source template-files build install reset



