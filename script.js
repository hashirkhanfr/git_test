let calculateButton = document.querySelector("#calculate");
let clearButton = document.querySelector("#clear");
let paragraph = document.querySelector("#result");
let submitButton = document.querySelector("#mySubmit");
console.log("Testing log");
let startingNumber;
let endingNumber;

submitButton.addEventListener("click",() => {
    startingNumber =  parseInt(document.querySelector("#start-number").value);
    endingNumber = parseInt(document.querySelector("#end-number").value);
});


function calculateSquares(){
    console.log("testing calculate button");
    paragraph.innerHTML = " ";
    for (let i = startingNumber; i <= endingNumber; i++){
        paragraph.innerHTML+=`${i} x ${i} = ${i*i} <br>`;
    }
}

calculateButton.addEventListener("click",calculateSquares());
clearButton.addEventListener("click",() => {paragraph.innerHTML="Cleared"});
