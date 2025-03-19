#!/bin/bash

ROOT_PATH=$(cat /etc/hammer/root)
JAR=hammer.jar
JAVA=java
PID=0
if [ -e $ROOT_PATH/runtime ]; then
	PID=$(cat $ROOT_PATH/runtime/PID)
	PORT=$(cat $ROOT_PATH/runtime/HTTP)
	TOKEN=$(cat $ROOT_PATH/runtime/TOKEN)
	kill -0 $PID > /dev/null 2>&1
	if [ $? != 0 ]; then
		PID=0
	fi
fi

function print_help {
	echo -e "\nUsage: hmr [command]"
	echo -e "\nCommands:"
	echo -e "  start\t\tStart the hammer in background"
	echo -e "  run\t\tStart the hammer in this session"
	echo -e "  stop\t\tStop the hammer"
	echo -e "  restart\tRestart the hammer"
	echo -e "  -s,status\tPrint hammer status"
	echo -e "  log\t\tPrint logs"
	echo -e "  -v,version\tPrint version information"
	echo -e "  -w,sites\tPrint websites list"
	echo -e "  -p,plugins\tPrint installed plugins list"
	echo -e "  reload\tReload websites and plugins"
	echo ""
}

function notrun {
	echo -e "The hammer is not running."
}

function ready {
	cd $ROOT_PATH
	cat /dev/urandom | tr -dc 'A-Za-z0-9' | head -c 16 > runtime/TOKEN
	TOKEN=$(cat runtime/TOKEN)
	rm -f logs/log
}

function stop {
	curl -s -H "Host: console..cmd" -H "Token: $TOKEN" localhost:$PORT/stop
}

if [ "$1" == "start" ]; then
	if [ $PID == 0 ]; then
		ready
		$JAVA -jar $JAR -install --token=$TOKEN > logs/log 2>&1 &
		disown
	fi
elif [ "$1" == "run" ]; then
	if [ $PID == 0 ]; then
		ready
		$JAVA -jar $JAR -install --token=$TOKEN
	else
		echo -e "The hammer is already running."
	fi
elif [ "$1" == "restart" ]; then
	stop
	ready
	$JAVA -jar $JAR -install --token=$TOKEN > logs/log 2>&1 &
	disown
elif [ "$1" == "stop" ]; then
	stop
elif [ "$1" == "status" ] || [ "$1" == "-s" ]; then
	if [ $PID == 0 ]; then
		notrun
	else
		echo $TOKEN
		echo -e "The hammer is running"
		echo -e "path:$ROOT_PATH"
		curl -H "Host: console..cmd" -H "Token: $TOKEN" localhost:$PORT/status
	fi
elif [ "$1" == "log" ]; then
	cat $ROOT_PATH/logs/log
elif [ "$1" == "plugins" ] || [ "$1" == "-p" ]; then
	if [ $PID == 0 ]; then
		notrun
	else
		curl -H "Host: console..cmd" -H "Token: $TOKEN" localhost:$PORT/plugins
	fi
elif [ "$1" == "-w" ] || [ "$1" == "sites" ]; then
	if [ $PID == 0 ]; then
		notrun
	else
		curl -H "Host: console..cmd" -H "Token: $TOKEN" localhost:$PORT/websites
	fi
elif [ "$1" == "reload" ]; then
	if [ $PID == 0 ]; then
		notrun
	else
		curl -H "Host: console..cmd" -H "Token: $TOKEN" localhost:$PORT/reload
	fi
elif [ "$1" == "version" ] || [ "$1" == "-v" ]; then
	cd $ROOT_PATH
	$JAVA -jar $JAR --version
else
	print_help
fi
