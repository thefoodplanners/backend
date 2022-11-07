function getTotalCalories() {
    var calorie1 = document.getElementById("calorie1").value;
    var calorie2 = document.getElementById("calorie2").value;
    fetch('http://localhost:9000/list-of-calories?calories=' + calorie1 + '&calories=' + calorie2)
    .then(response => response.text())
    .then(data => displayTotalCalories(data))
}

function displayTotalCalories(total) {
    var totalCalories = document.getElementById("total");
    totalCalories.innerHTML = "Total amount of calories: " + total;
}