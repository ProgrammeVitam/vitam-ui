# Args parsing
JAR_FILE         := $(NAME)-$(VERSION).jar
DEPENDENCIES     := systemd


# Staging root configuration
DESTDIR         := $(abspath ./package-stage/)
STAGING_ROOT    =  $(abspath $(DESTDIR)/)
FPM             =  /usr/local/bin/fpm
USER            := vitamui
GROUP           := $(USER)

# Package metas
COPYRIGHT           = VitamUI Copyright 2018 VITAM_UI
COMMIT_ID           = $(shell  git rev-parse --short HEAD)
BRANCHE             = $(shell  git rev-parse --abbv6L6zFUCr82Bjy9Qrev-ref HEAD)
DESCRIPTION         = $(NAME) package for vitamui solution. Branch: $(BRANCHE), commit: $(COMMIT_ID)

# Install params
SYSTEMD_DIR         := /usr/lib/systemd/system/
VITAMUI_DIRS           =  app bin lib script conf log data tmp run defaults
PACKAGING_DIR       := $(dir $(lastword $(MAKEFILE_LIST)))
TEMPLATE_TMP_DIR    := $(abspath ./package-templates/)
TEMPLATE_SRC        = $(wildcard $(PACKAGING_DIR)/templates/*)
TEMPLATE_SRC_OVERRIDE = $(wildcard $(shell pwd)/../packaging/*)
RPM_OUTPUT_PATH     := vitamui-$(NAME)-$(VERSION).rpm

# fpm packaging default args
FPM_PACK_ARGS               = -f -d "$(DEPENDENCIES)" --description "$(DESCRIPTION)"
FPM_PACK_META               = --license "$(COPYRIGHT)" -n "vitamui-$(NAME)" -v $(VERSION)
FPM_PACK_INSTALL_SCRIPTS    = --before-install "$(TEMPLATE_TMP_DIR)/before-install.sh" --after-install "$(TEMPLATE_TMP_DIR)/after-install.sh"
FPM_PACK_REMOVE_SCRIPTS     = --before-remove "$(TEMPLATE_TMP_DIR)/before-remove.sh" --after-remove "$(TEMPLATE_TMP_DIR)/after-remove.sh"


clean:
	@rm -Rf $(TEMPLATE_TMP_DIR) $(STAGING_ROOT)
	@rm -Rf $(TEST_DIR)
	@rm -f  $(RPM_OUTPUT_PATH)


$(STAGING_ROOT):
	@mkdir -p $(STAGING_ROOT)


stage: $(STAGING_ROOT)


$(VITAMUI_DIRS): stage
	@mkdir -p $(abspath $(STAGING_ROOT)/vitamui/$@/$(NAME))


vitamui-dirs:  $(VITAMUI_DIRS)


template-files:
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
	# Processing local overrides:
	@for fic in  $(TEMPLATE_SRC_OVERRIDE) ; \
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

install: clean stage vitamui-dirs template-files
	@# install jar file in /vitamui/app/_NAME_/ with right mode
	@mkdir   -p -m 750 $(abspath $(STAGING_ROOT)/vitamui/app/$(NAME)/)
	@install -m 644 $(JAR_FILE) $(abspath $(STAGING_ROOT)/vitamui/app/$(NAME)/$(JAR_FILE))
	@# Install cmd_line_args file  in /vitamui/conf/$(NAME)/sysconfig/
	@mkdir   -p -m 750 $(abspath $(STAGING_ROOT)/vitamui/conf/$(NAME)/sysconfig/)
	@install -m 640 $(TEMPLATE_TMP_DIR)/sysconfig.cmd_line_args $(abspath $(STAGING_ROOT)/vitamui/conf/$(NAME)/sysconfig/cmd_line_args)
	@install -m 640 $(TEMPLATE_TMP_DIR)/sysconfig.java_opts $(abspath $(STAGING_ROOT)/vitamui/conf/$(NAME)/sysconfig/java_opts)
	@# Install systemd service file
	@mkdir   -p -m 750 $(abspath $(STAGING_ROOT)/$(SYSTEMD_DIR)/)
	@install -m 644 $(TEMPLATE_TMP_DIR)/vitamui.service $(abspath $(STAGING_ROOT)/$(SYSTEMD_DIR)/vitamui-$(NAME).service)



rpm: install
	@$(FPM) -s dir $(FPM_PACK_ARGS) $(FPM_PACK_META) $(FPM_PACK_INSTALL_SCRIPTS) $(FPM_PACK_REMOVE_SCRIPTS) -t rpm -p $(RPM_OUTPUT_PATH)  "$(STAGING_ROOT)/=/"


.PHONY: install clean stage vitamui-dirs install-service rpm package template-files $(TEMPLATE_TMP_DIR)
