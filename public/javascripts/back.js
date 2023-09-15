(function(document, window) {
    const backLink = document.getElementById("js-back-link")
    if(backLink) {
        backLink.classList.remove("js-hidden")
        backLink.addEventListener('click', function(e){
            e.preventDefault()
            window.history.back()
        })
    }
})(document, window)

