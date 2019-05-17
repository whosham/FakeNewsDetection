<?php

$command="ls"
exec("$command 2>&1 &", $output);
foreach ($output as $line) {
    echo "$line\n";
}

?>