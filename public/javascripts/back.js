$(document).ready(function() {
    $("#back").removeClass("js-hidden");
    $('#back-link').on('click', function(e){
        e.preventDefault();
        window.history.back();
    })
});