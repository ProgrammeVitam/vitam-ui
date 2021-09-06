/* global jqueryReady, policyPattern, zxcvbn, passwordStrengthI18n */
/*eslint-disable no-unused-vars*/
function jqueryReady() {
    var strength = passwordStrengthI18n;

    $.fn.zxcvbnProgressBar = function (options) {

        //init settings
        var settings = $.extend({
            allProgressBarClasses: 'progress-bar-danger progress-bar-warning progress-bar-success progress-bar-striped active',
            progressBarClass0: 'progress-bar-danger progress-bar-striped active',
            progressBarClass1: 'progress-bar-danger progress-bar-striped active',
            progressBarClass2: 'progress-bar-warning progress-bar-striped active',
            progressBarClass3: 'progress-bar-success',
            progressBarClass4: 'progress-bar-success'
        }, options);

        return this.each(function () {
            settings.progressBar = this;
            //init progress bar display
            UpdateProgressBar();
            //Update progress bar on each keypress of password input
            $(settings.passwordInput).keyup(function (event) {
                UpdateProgressBar();
            });
        });

        function UpdateProgressBar() {
            var progressBar = settings.progressBar;
            var password = $('#password').val();
            if (password) {
                var result = zxcvbn(password, settings.userInputs);
                //result.score: 0, 1, 2, 3 or 4 - if crack time is less than 10**2, 10**4, 10**6, 10**8, Infinity.
                var scorePercentage = (result.score + 1) * 20;
                $(progressBar).css('width', scorePercentage + '%');

                if (result.score == 0) {
                    //weak
                    $(progressBar).removeClass(settings.allProgressBarClasses).addClass(settings.progressBarClass0);
                    $(progressBar).html(strength[0]);
                }
                else if (result.score == 1) {
                    //normal
                    $(progressBar).removeClass(settings.allProgressBarClasses).addClass(settings.progressBarClass1);
                    $(progressBar).html(strength[1]);
                }
                else if (result.score == 2) {
                    //medium
                    $(progressBar).removeClass(settings.allProgressBarClasses).addClass(settings.progressBarClass2);
                    $(progressBar).html(strength[2]);
                }
                else if (result.score == 3) {
                    //strong
                    $(progressBar).removeClass(settings.allProgressBarClasses).addClass(settings.progressBarClass3);
                    $(progressBar).html(strength[3]);
                }
                else if (result.score == 4) {
                    //very strong
                    $(progressBar).removeClass(settings.allProgressBarClasses).addClass(settings.progressBarClass4);
                    $(progressBar).html(strength[4]);
                }
            }
            else {
                $(progressBar).css('width', '0%');
                $(progressBar).removeClass(settings.allProgressBarClasses).addClass(settings.progressBarClass0);
                $(progressBar).html('');
            }
        }
    };
    ///
    var policyPatternRegex = new RegExp(policyPattern);
    var password = document.getElementById('password');
    var confirmed = document.getElementById('confirmedPassword');

    password.addEventListener('input', validate);
    confirmed.addEventListener('input', validate);

    var alertSettings = {
        allAlertClasses: 'fa-times-circle fa-exclamation-circle fa-info-circle fa-check-circle',
        alertClassDanger: 'fa-times-circle',
        alertClassWarning: 'fa-exclamation-circle',
        alertClassInfo: 'fa-info-circle',
        alertClassSuccess: 'fa-check-circle'
    };

    function validate() {
        var val = password.value;
        var cnf = confirmed.value;
        var responseText;
        var passwordIsNotValid = !policyPatternRegex.test(val);

        var disableSubmit = val == '' || cnf == '' || val != cnf || passwordIsNotValid || !policyPatternRegex.test(cnf);
        $('#submit').prop('disabled', disableSubmit);

        var result = zxcvbn(val);
        $('#strengthProgressBar').zxcvbnProgressBar({ passwordInput: '#password' });

        if (passwordIsNotValid && $('#security-four.valid').length > 0 && $('#security-valid-password.valid').length > 0) {
          //$('#password-policy-text').show();
          // $('#password-helper').show();

          $('#security-invalid-password').addClass("valid");
          $('#security-invalid-password').show();
        } else {
          $('#password-policy-text').hide();
          $('#security-valid-password').show();
        }

        if (disableSubmit) {
           return;
        }
    }
}
