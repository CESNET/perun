<?php

header("Content-Type: image/png");

define("_WIDTH", 10);
define("_HEIGHT", 10);


if (empty($_GET["color"])) exit();

$color = htmlspecialchars(urldecode($_GET["color"]));
$w = @abs(intval(htmlspecialchars($_GET["w"]))) | _WIDTH;
$h = @abs(intval(htmlspecialchars($_GET["h"]))) | _HEIGHT;
$file = "bullet-${color}-${w}-${h}.png";


if(file_exists($file)) {
	echo file_get_contents($file);
	exit();
}

include "bullet-generator.inc.php";

$colorParsed = hex2RGB($color);
if($colorParsed === false) exit();

$bullet = generateBullet($colorParsed, $w, $h);
echo $bullet;
file_put_contents($file, $bullet);