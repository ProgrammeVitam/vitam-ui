function preserveAnchorTagOnForm() {
  $('#main-form').submit(function () {
    var location = self.document.location;
    var hash = decodeURIComponent(location.hash);
    if (hash !== undefined && hash !== '' && hash.indexOf('#') === -1) {
      hash = '#' + hash;
    }
    var action = $('#main-form').attr('action');
    if (action === undefined) {
      action = location.href;
    } else {
      var qidx = location.href.indexOf('?');
      if (qidx !== -1) {
        var queryParams = location.href.substring(qidx);
        action += queryParams;
      }
    }
    action += hash;
    $('#main-form').attr('action', action);
  });
}

function preventFormResubmission() {
  $('form').submit(function () {
    $(':submit').attr('disabled', true);
    var altText = $(':submit').attr('data-processing-text');
    if (altText) {
      $(':submit').attr('value', altText);
    }
    return true;
  });
}

// Customization VITAMUI =======================
function disableEmptyInputFormSubmission() {
  var fields = $('#main-form input[name="username"],[name="password"]');
  if (fields.length === 2) {
    fields.on('input', function (event) {
      var enableSubmission = $('#main-form input[name="username"]').val().trim() &&
        $('#main-form input[name="password"]').val().trim();

      if (enableSubmission) {
        $('#main-form input[name=submit]').removeAttr('disabled');
        event.stopPropagation();
      } else {
        $('#main-form input[name=submit]').attr('disabled', 'true');
      }
    });
  }

  /**
   * Handle auto-complete events to the extent possible.
   */
  if ($('#main-form input[name="username"]').length > 0) {
    setTimeout(function () {
      var uid = $('#username').val();
      if (uid != null && uid != '') {
        $('#username').change();
        $('#username').focus();
        $('#main-form input[name=submit]').removeAttr('disabled');
      }

    }, 100);
  }
}

// End of Customization VITAMUI =======================

function resourceLoadedSuccessfully() {
  $(document).ready(function () {


    if ($(':focus').length === 0) {
      $('input:visible:enabled:first').focus();
    }

    // Customization VITAMUI =======================
    disableEmptyInputFormSubmission();
    // End of Customization VITAMUI =======================
    preserveAnchorTagOnForm();
    preventFormResubmission();

    $('#capslock-on').hide();
    $('#main-form input[name="username"],[name="password"]').trigger('input');
    $('#main-form input[name="username"]').focus();
    $('#password').keypress(function (e) {
      var s = String.fromCharCode(e.which);
      if (s.toUpperCase() === s && s.toLowerCase() !== s && !e.shiftKey) {
        $('#capslock-on').show();
      } else {
        $('#capslock-on').hide();
      }
    });
    if (typeof (jqueryReady) == 'function') {
      console.log("jqueryReady is a function")
      jqueryReady();
    } else {
      console.log("jqueryReady not a function")
    }
  });

}
