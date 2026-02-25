/* global trackGeoLocation, jqueryReady */

/* exported resourceLoadedSuccessfully */

const formId = '#main-form'; // Vitam form id value
// const formId = '#fm1'; // Xelians form id value

function showGeoPosition(position) {
  let loc =
    position.coords.latitude +
    ',' +
    position.coords.longitude +
    ',' +
    position.coords.accuracy +
    ',' +
    position.timestamp;
  console.log('Tracking geolocation for ' + loc);
  $('[name="geolocation"]').val(loc);
}

function preserveAnchorTagOnForm() {
  $(formId).submit(function () {
    var location = self.document.location;
    var hash = decodeURIComponent(location.hash);

    if (hash !== undefined && hash !== '' && hash.indexOf('#') === -1) {
      hash = '#' + hash;
    }

    var action = $(formId).attr('action');
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
    $(formId).attr('action', action);
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
  var fields = $(`${formId} input[name="username"],[name="password"]`);
  if (fields.length === 2) {
    fields.on('input', function (event) {
      var enableSubmission =
        $(`${formId} input[name="username"]`).val().trim() &&
        $(`${formId} input[name="password"]`).val().trim();

      if (enableSubmission) {
        $(`${formId} input[name=submit]`).removeAttr('disabled');
        event.stopPropagation();
      } else {
        $(`${formId} input[name=submit]`).attr('disabled', 'true');
      }
    });
  }

  /**
   * Handle auto-complete events to the extent possible.
   */
  if ($(`${formId} input[name="username"]`).length > 0) {
    setTimeout(function () {
      var uid = $('#username').val();
      if (uid != null && uid != '') {
        $('#username').change();
        $('#username').focus();
        $(`${formId} input[name=submit]`).removeAttr('disabled');
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
    $(`${formId} input[name="username"],[name="password"]`).trigger('input');
    $(`${formId} input[name="username"]`).focus();

    $('#password').keypress(function (e) {
      var s = String.fromCharCode(e.which);
      if (s.toUpperCase() === s && s.toLowerCase() !== s && !e.shiftKey) {
        $('#capslock-on').show();
      } else {
        $('#capslock-on').hide();
      }
    });
    if (typeof jqueryReady == 'function') {
      console.log('jqueryReady is a function');
      jqueryReady();
    } else {
      console.log('jqueryReady not a function');
    }
  });
}

function displayMainFormSubmitButton() {
  $(document).ready(function () {
    $('#main-form-submit').css('display', 'flex');
  });
}

function displayMainFormValidateButton() {
  $(document).ready(function () {
    $('#main-form-validate').css('display', 'flex');
  });
}

function disableMainFormSubmitButton() {
  $('#main-form-submit').attr('disabled', 'true');
}

function enableMainFormSubmitButton() {
  $('#main-form-submit').removeAttr('disabled');
}

function hiddeMainFormReturnButton() {
  $(document).ready(function () {
    $('#main-form-return').hide();
  });
}

function sanitizeUrl(inputUrl) {
  let ESC_MAP = {
    '&': '&amp;',
    '<': '&lt;',
    '>': '&gt;',
    '"': '&quot;',
    "'": '&#39;',
  };
  return inputUrl.replace(true ? /[&<>'"]/g : /[&<>]/g, function (c) {
    return ESC_MAP[c];
  });
}
