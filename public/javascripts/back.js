(function(document, window) {
    const back = document.getElementById("back")
    const backLink = document.getElementById("back-link")
    if(back && backLink) {
        back.classList.remove("js-hidden")
        backLink.addEventListener('click', function(e){
            e.preventDefault()
            window.history.back()
        })
    }
})(document, window)

