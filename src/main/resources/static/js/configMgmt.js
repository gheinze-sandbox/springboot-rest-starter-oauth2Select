

// Bootstrap

$(document).ready(function () {
    appModel.init();
});


// Model definition

var appModel = {};


// Init
(function(exports) {

    exports.init = function() {
        exports.queryForUser();
    };

})(appModel);



// User Controller

(function(exports) {

    exports.userName = "";
    var loggedIn = false;


    exports.queryForUser = function () {
        $.ajax({
            url: "/user"
        }).then(function (data) {
            loggedIn = true;
            updateUserName(data);
        }).fail(function () {
            loggedIn = false;
            updateUserName("");
        });
    };

    exports.logout = function() {
        $.ajax({
            url: '/logout',
            headers: {
                'X-XSRF-TOKEN': exports.getXsrfToken()
            },
            method: 'POST',
            success: function (data) {
                loggedIn = false;
                updateUserName("");
            }
        });
      };

    function updateUserName(newUserName) {
        exports.userName = newUserName;
        $("#userNameId").text(newUserName);
        if (loggedIn) {
            $(".authenticatedState").show();
            $(".nonAuthenticatedState").hide();
        } else {
            $(".authenticatedState").hide();
            $(".nonAuthenticatedState").show();
        }
    };

})(appModel);



// EBlox Upload Controller

(function(exports) {

    exports.uploadEbloxFile = function() {
        if (!validEbloxUploadData()) {
            alert("Please select a file for upload");
            return;
        }
        var ebloxUploadFormData = createEbloxUploadForm();
        postEbloxUploadForm(ebloxUploadFormData);
    };


    function createEbloxUploadForm() {
        var formData = new FormData();
        formData.append("_csrf", exports.getXsrfToken());
        formData.append("file", $("#fileInputId")[0].files[0]);
        return formData;
    };


    function validEbloxUploadData() {
        var file = $("#fileInputId")[0].files[0];
        return !(file === undefined || file === null);
    };


    function postEbloxUploadForm(ebloxUploadFormData) {
        jQuery.ajax({
            url: "/api/upload/eblox",
            data: ebloxUploadFormData,
            cache: false,
            contentType: false,  // otherwise jQuery will implicitly addd content-type
            processData: false,  // otherwise jQuery will try to change data to a String
            type: 'POST',
            success: function(data) {
                if (data.successful) {
                    logSuccessfulEbloxUpload(data.msg);
                } else {
                    alert(data.msg);
                }
            },
            failure: function(data) {
                alert(data.msg);
            }
        });
    };


    function logSuccessfulEbloxUpload(selectedFileName) {
        //selectedFileName = $("#fileInputId").val().split('/').pop().split('\\').pop();
        $("#uploadMessageId").text(selectedFileName);
        $("#uploadMessageDivId").slideDown(function() {
            setTimeout(function() {
                $("#uploadMessageDivId").slideUp();
            }, 5000);
        });
        $("#fileInputId").val("");
        $("#uploadedListId").append("<li>" + selectedFileName + "</li>");
    }


    exports.getXsrfToken = function() {
        var re = new RegExp("XSRF-TOKEN=([^;]+)");
        var value = re.exec(document.cookie);
        return (value !== null) ? unescape(value[1]) : null;
    };

    exports.setXsrf = function() {
        document.getElementById("xsrfId").value = exports.getXsrfToken();
        return true;
    };


})(appModel);

