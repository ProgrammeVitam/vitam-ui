#
# Makefile for packaging COTS component of vitamui solution
#


# Args parsing
NAME                := logstash
VERSION             := 0.0.0
LOGSTASH_VERSION    := 7.6.0
DEPENDENCIES        := systemd,java-11-openjdk


# Src configuration:
SRC_DIR             := $(abspath  ./src)
SRC_ARCHIVE         = logstash-$(LOGSTASH_VERSION).tar.gz
SRC_URL             := https://artifacts.elastic.co/downloads/logstash/$(SRC_ARCHIVE)
BIN                 =  $(abspath  $(SRC_DIR)/usr/share/logstash/bin/logstash )


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
DESCRIPTION         = $(NAME) package for vitamui solution. Made from logstash versio $(LOGSTASH_VERSION) Branch: $(BRANCHE), commit: $(COMMIT_ID)


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
	@rm -Rf src/


#
# COTS src fetching:
#

# Prepare src dir
$(SRC_DIR):
	@mkdir -p $(SRC_DIR)

# Fetch src archive in $(SRC_DIR)
$(SRC_DIR)/$(SRC_ARCHIVE): $(SRC_DIR)
	@[ -f $(SRC_DIR)/$(SRC_ARCHIVE) ] || curl --silent --fail $(SRC_URL) -o $(SRC_DIR)/$(SRC_ARCHIVE) || echo "Download $(SRC_URL) in $(SRC_DIR)/$(SRC_ARCHIVE) failed  "

# Extract RPM to get pre  staged install files from logstash
$(BIN): $(SRC_DIR)/$(SRC_ARCHIVE)
	@cd $(SRC_DIR) ; tar xvzf $(SRC_ARCHIVE)

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

install: source stage vitamui-dirs template-files
	# Installing usr/ directory of logstash RPM in package stage root
	@cp -Ru $(SRC_DIR)/logstash-$(LOGSTASH_VERSION)/* $(abspath $(STAGING_ROOT)/vitamui/app/$(NAME))
	# Install config files
	@mkdir   -p -m 750 $(abspath $(STAGING_ROOT)/vitamui/conf/$(NAME)/conf.d)
	@install -m 644 $(TEMPLATE_TMP_DIR)/jvm.options $(abspath $(STAGING_ROOT)/vitamui/conf/$(NAME)/jvm.options)
	@install -m 644 $(TEMPLATE_TMP_DIR)/log4j2.properties $(abspath $(STAGING_ROOT)/vitamui/conf/$(NAME)/log4j2.properties)
	@install -m 644 $(TEMPLATE_TMP_DIR)/logstash.yml $(abspath $(STAGING_ROOT)/vitamui/conf/$(NAME)/logstash.yml)
	@install -m 644 $(TEMPLATE_TMP_DIR)/startup.options $(abspath $(STAGING_ROOT)/vitamui/conf/$(NAME)/startup.options)
	# Install sysconfig file
	@mkdir   -p -m 750 $(abspath $(STAGING_ROOT)/vitamui/conf/$(NAME)/sysconfig)
	@install -m 644 $(TEMPLATE_TMP_DIR)/sysconfig_logstash $(abspath $(STAGING_ROOT)/vitamui/conf/$(NAME)/sysconfig/logstash)
	# Install systemd unit file
	@mkdir   -p -m 750 $(abspath $(STAGING_ROOT)/usr/lib/systemd/system/)
	@install -m 644 $(TEMPLATE_TMP_DIR)/vitamui.service $(abspath $(STAGING_ROOT)/$(SYSTEMD_DIR)/vitamui-logstash.service)


rpm: install
	@$(FPM) -s dir $(FPM_PACK_ARGS) $(FPM_PACK_META) $(FPM_PACK_INSTALL_SCRIPTS) $(FPM_PACK_REMOVE_SCRIPTS) -t rpm -p $(RPM_OUTPUT_PATH)  "$(STAGING_ROOT)/=/"

#
# COTS Packaging
#ec

.PHONY: clean stage vitamui-dirs source template-files build install reset



