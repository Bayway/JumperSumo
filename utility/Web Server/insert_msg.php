<?php
$servername = "localhost";
$username = "baywayla_jumper";
$password = "jumpersumo!!7";
$dbname = "baywayla_jumper";

$MSG = $_GET['text'];

// Create connection
$conn = new mysqli($servername, $username, $password, $dbname);
// Check connection
if ($conn->connect_error) {
    die("Connection failed: " . $conn->connect_error);
} 

$sql = "INSERT INTO commands (text)
VALUES ('".$MSG."')";

if ($conn->query($sql) === TRUE) {
    echo "1";
} else {
    echo "Error: " . $sql . "<br>" . $conn->error;
}

$conn->close();
?>