#
# Makefile for packaging COTS component of vitamui solution
#

# Args parsing with default values
NAME                := mongod
VERSION             := 3.4.9
DEPENDENCIES        := mongodb-org

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
DESCRIPTION         = $(NAME) package for vitamui solution. Mongod version: latest available in guest system. If setup from mongodb official repository, check your yum.repos.d. Branch: $(BRANCHE), commit: $(COMMIT_ID)

# Install params
SYSTEMD_DIR         := /usr/lib/systemd/system/
VITAMUI_DIRS           =  app bin lib script conf log data tmp run defaults
PACKAGING_DIR       := $(dir $(lastword $(MAKEFILE_LIST)))
TEMPLATE_TMP_DIR    := $(abspath ./package-templates/)
TEMPLATE_SRC        = $(wildcard $(PACKAGING_DIR)/templates/*)
DEB_OUTPUT_PATH     := vitamui-$(NAME)-$(VERSION).deb
RPM_OUTPUT_PATH     := vitamui-$(NAME)-$(VERSION).rpm

# fpm packaging default args
FPM_PACK_ARGS               = -f -d "$(DEPENDENCIES)" --description "$(DESCRIPTION)"
FPM_PACK_META               = --license "$(COPYRIGHT)" -n "vitamui-$(NAME)" -v $(VERSION)
FPM_PACK_INSTALL_SCRIPTS    = --before-install "$(TEMPLATE_TMP_DIR)/before-install.sh" --after-install "$(TEMPLATE_TMP_DIR)/after-install.sh"
FPM_PACK_REMOVE_SCRIPTS     = --before-remove "$(TEMPLATE_TMP_DIR)/before-remove.sh" --after-remove "$(TEMPLATE_TMP_DIR)/after-remove.sh"

#
# Imports from packaging Makefiles
#

stage:
	$(MAKE) -f $(PACKAGING_MAKEFILE) NAME=$(NAME) VERSION=$(VERSION) STAGING_ROOT=$(STAGING_ROOT) $@

vitamui-dirs:
	$(MAKE) -f $(PACKAGING_MAKEFILE) NAME=$(NAME) VERSION=$(VERSION) $@

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

install: stage vitamui-dirs template-files
	@install -m 644 $(TEMPLATE_TMP_DIR)/$(NAME).conf.default $(abspath $(STAGING_ROOT)/vitamui/defaults/$(NAME)/$(NAME).conf)

	@mkdir   -p -m 750 $(abspath $(STAGING_ROOT)/etc/logrotate.d)
	@install -m 644 $(TEMPLATE_TMP_DIR)/logrotated.mongod $(abspath $(STAGING_ROOT)/etc/logrotate.d/logrotated.mongod)

	@mkdir   -p -m 750 $(abspath $(STAGING_ROOT)/$(SYSTEMD_DIR)/)
	@install -m 644 $(TEMPLATE_TMP_DIR)/vitamui-mongod.service $(abspath $(STAGING_ROOT)/$(SYSTEMD_DIR)/vitamui-mongod.service)


rpm: install
	@$(FPM) -s dir $(FPM_PACK_ARGS) $(FPM_PACK_META) $(FPM_PACK_INSTALL_SCRIPTS) $(FPM_PACK_REMOVE_SCRIPTS) -t rpm -p $(RPM_OUTPUT_PATH)  "$(STAGING_ROOT)/=/"


deb: install
	@$(FPM) -s dir $(FPM_PACK_ARGS) $(FPM_PACK_META) $(FPM_PACK_INSTALL_SCRIPTS) $(FPM_PACK_REMOVE_SCRIPTS) -t deb -p $(DEB_OUTPUT_PATH)  "$(STAGING_ROOT)/=/"

.PHONY: stage vitamui-dirs deb rpm source template-files build install reset
