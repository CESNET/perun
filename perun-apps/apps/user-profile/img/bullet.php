<?php

header("Content-Type: image/png");

if (empty($_GET["color"])) {
    exit();
}

$color = htmlspecialchars(urldecode($_GET["color"]));
$w = @abs(intval(htmlspecialchars($_GET["w"]))) | 10;
$h = @abs(intval(htmlspecialchars($_GET["h"]))) | 10;
$file = "bullet-${color}-${w}-${h}.png";


if (file_exists($file)) {
    echo file_get_contents($file);
    exit();
}

include "bullet-generator.inc.php";

$colorParsed = hex2RGB($color);
if ($colorParsed === false) {
    exit();
}

$bullet = generateBullet($colorParsed, $w, $h);
echo $bullet;
file_put_contents($file, $bullet);
