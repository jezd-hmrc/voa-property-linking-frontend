(function() {

    'use strict';

    var root = this, $ = root.jQuery;

    if (typeof VOA === 'undefined') {
        root.VOA = {};
    }

    var FileUpload = function (){
        var $element = $('#newFile');

        var errorMessages = '<div id="error-summary" class="error-summary error-summary--show" role="group" aria-labelledby="error-summary-heading" tabindex="-1">'+
            '<h2 class="heading-medium error-summary-heading" id="error-summary-heading">There&rsquo;s been a problem</h2>'+
            '<p>Check the following</p>'+
            '<ul class="error-summary-list"><li></li></ul></div>';

        $element.change(function(){
            $('#error-summary').remove();
            // $('#message-warning').remove();
            $('#saveAndContinueButton').attr('disabled','disabled');

            $(this).after('<div class="message-warning" id="message-warning"><p>Please wait whilst your file is uploading. This may take some time.</p></div>');
            var file = this.files[0];
            var csrfToken = $("#uploadForm input[name='csrfToken']").val();
            var fileName = file.name.replace(/[^0-9A-Za-z. -]/g,' ');
            var submissionId = $("#submissionId").text();
            //$(this).closest('form').submit();
            $.ajax({
                url: $("#businessRatesAttachmentsInitiateUploadURL").text(),
                method: "POST",
                contentType: "application/json",
                data: JSON.stringify({
                    "fileName": submissionId + "-" + fileName,
                    "mimeType": file.type,
                    "csrfToken": csrfToken
                }),
                headers: {
                    'Csrf-Token': csrfToken
                },
                crossDomain: false,
                cache: false
            }).error(function(jqXHR, textStatus, errorThrown ){
                if(jqXHR.status == 400) {
                    $('#errorsList').html(errorMessages.replace('<li></li>', '<li>'+ $('#errorsBusinessRatesAttachmentUnsupportedFiles').text()+'</li>'));
                    $('#message-warning').remove();
                }else if(jqXHR.status > 400) {
                    $('#errorsList').html(errorMessages.replace('<li></li>', '<li>'+ $('#errorsBusinessRatesAttachmentUnavailable').text()+'</li>'));
                    $('#message-warning').remove();
                }

            }).done(function(data, statusText, resObject) {
                fileUpload(resObject.responseJSON, file, csrfToken);
                $('#message-warning').remove();
            });

            $(this).closest('.form-group').removeClass('error');
            $(this).closest('.form-group').find('.error-message').remove();
            //$('#newFileButton').remove();
        });

        $element.attr({'tabindex':'-1', 'style': 'position: absolute; left: -9999px; top: -9999px; z-index: -9999'});
        // $element.after('<a id="newFileButton" href="#" class="button-secondary">Choose a file to upload</a>');
        $('[for="newFile"] .label-span').addClass('visuallyhidden');

        $(document).on('click', '#newFileButton', function(e){
            e.preventDefault();
            $element.trigger('click');
        });


        function removeUploadFileOnError(fileReference, csrfToken, upScanError){
            $.ajax({
                url: $("#businessRatesAttachmentsRemoveFileURL").text()+fileReference,
                method: "POST",
                data: JSON.stringify({"fileName": fileReference}),
                "csrfToken": csrfToken,
                headers: {
                    'Csrf-Token': csrfToken
                },
                crossDomain: false,
                cache: false
            }).error(function(jqXHR, textStatus, errorThrown ){

                if(jqXHR.status == 400 || upScanError.status == 400) {
                    $('#errorsList').html(errorMessages.replace('<li></li>', '<li>'+ $('#errorsBusinessRatesAttachmentUnsupportedFiles').text()+'</li>'));
                }else if(jqXHR.status > 400) {
                    $('#errorsList').html(errorMessages.replace('<li></li>', '<li>'+ $('#error.upscan.unavailable').text()+'</li>'));
                }
                $('#message-warning').remove();
            }).done(function(data, statusText, resObject) {
                $('#errorsList').html(errorMessages.replace('<li></li>', '<li>'+ $('#errorsBusinessRatesAttachmentUnsupportedFiles').text()+'</li>'));
                $('#message-warning').remove();
            });
        }
        

        function fileUpload(form, file, csrfToken){
            var data = new FormData();

            Object.keys(form.uploadRequest.fields).map(function(k) {
                data.append(k, form.uploadRequest.fields[k]);
            });

            data.append("file", file);

            $.ajax({
                url: form.uploadRequest.href,
                type: "POST",
                data: data,
                processData: false,
                contentType: false,
                crossDomain: true,
                cache: false,
                enctype: 'multipart/form-data'
            }).error(function(jqXHR, textStatus, errorThrown ){
                    removeUploadFileOnError(form.reference, csrfToken, jqXHR)
            }).done(function(){
                window.location = $("#businessRatesAttachmentsFileUploadURL").text();
                $('#message-warning').remove();
            });
        };

    };



    root.VOA.FileUpload = FileUpload;

}).call(this);