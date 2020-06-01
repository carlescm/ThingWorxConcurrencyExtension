echo -n -e "\033]0;Packing ConcurrencySEE\007"
cd "`dirname "$0"`"
ant
#osascript -e 'tell application "Terminal" to close (every window whose name contains "Packing ConcurrencySEE")' &