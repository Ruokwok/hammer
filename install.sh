#!/bin/bash

REPO=https://hmr.starelement.net

function error {
	echo -e "[\033[31mERROR\033[0m]$1"
}

function info {
	echo -e "[\033[32mINFO \033[0m]$1"
}

function install {
	if [ $HMR_V != 0 ]; then
		error "Hammer is already installed."
		exit 5
	fi
	type curl > /dev/null 2>&1
	CURL=$?
	if [ $CURL != 0 ]; then
		echo -e "[ERROR] The curl is not installed."
		echo -e "[ERROR] Please install curl and try again."
		exit 1
	fi
	info "Please input the Java path:"
	read -p "(java):" JAVA
	if [ -z $JAVA ]; then
		JAVA=java
	fi
	$JAVA -version > /dev/null 2>&1
	if [ $? != 0 ]; then
		error " This Java program error."
		exit 1
	fi
	$JAVA > /dev/null 2>&1
	if [ $? != 1 ]; then
		echo -e "[ERROR] This Java program error."
		exit 1
	fi
	JAVA_VERSION=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}')
	V=$(echo $JAVA_VERSION | awk -F '.' '{print $1}')
	if [ $V -lt 17 ]; then
		echo -e "Java version requires a minimum of 17, currently 11($JAVA_VERSION)."
		exit 1;
	fi
	echo $JAVA > /etc/hammer/java.info
	info "Please input hammer install path:"
	read -p "(/opt/hammer):" RI
	if [ -z $RI ]; then
		RI=/opt/hammer
	fi
	mkdir -p $RI
	cd $RI
	ROOT=$(pwd)
	echo $ROOT > /etc/hammer/root
	info "Donwload file..."
	curl -v -L -o $ROOT/hammer.jar $REPO/hammer.jar
	curl -v -L -o $ROOT/hmr.sh $REPO/hmr.sh
	chmod 755 $ROOT/hmr.sh
	ln -s $ROOT/hmr.sh /bin/hmr
	hmr version > /dev/null
	if [ $? == 0 ]; then
		info "Download done."
		hmr version
		info "Success!"
		hmr
	fi
}

function uninstall {
	info "Do you want to keep site directory? ($HAMMER_ROOT/wwwroot/)"
	read -p "(yes/no):" KEEP
	if [ $KEEP == "yes" ]; then
		stop_hammer
		rm -drv /bin/hmr
		rm -drv /etc/hammer
		mv $HAMMER_ROOT/wwwroot /tmp/wwwroot
		rm -drv $HAMMER_ROOT
		mkdir -p $HAMMER_ROOT
		mv /tmp/wwwroot $HAMMER_ROOT/wwwroot
		info "Done."
	elif [ $KEEP == "no" ]; then
		stop_hammer
		rm -drv /bin/hmr
		rm -drv /etc/hammer
		rm -drv $HAMMER_ROOT
		info "Done."
	else
		error "Invalid input."
	fi
}

function update {
	type hmr > /dev/null 2>&1
	if [ $? == 0 ]; then
		info "Check version update..."
		JAVA=$(cat /etc/hammer/java.info)
		THIS=$($JAVA -jar $HAMMER_ROOT/hammer.jar --version=simple)
		LATEST=$(curl -s $REPO/version)
		info "Installed version: $THIS"
		info "Latest version: $LATEST"
		if [ $THIS == $LATEST ]; then
			info "Already is latest"
			info "Do you want to force an update?"
			read -p "(no/yes):" FORCE
			if [ $FORCE != "yes" ]; then
				exit 0;
			fi
		fi
		stop_hammer
		rm -rv $HAMMER_ROOT/hammer.jar
		rm -rv $HAMMER_ROOT/hmr.sh
		curl -v -L -o $HAMMER_ROOT/hammer.jar $REPO/hammer.jar
		curl -v -L -o $HAMMER_ROOT/hmr.sh $REPO/hmr.sh
		info "Download success."
		chmod 755 $HAMMER_ROOT/hmr.sh
		hmr version
		info "Done."
	else
		error "Hammer is not installed, please usage install command."
	fi
}

function stop_hammer {
	if [ -f "$HAMMER_ROOT/runtime/PID" ]; then
		kill $(cat $HAMMER_ROOT/runtime/PID)
	fi
}

info "Hammer Manager installer"
mkdir -p /etc/hammer
HMR_V=0
echo
type hmr > /dev/null 2>&1
if [ $? == 0 ]; then
	JAVA_PATH=$(cat /etc/hammer/java.info)
	HAMMER_ROOT=$(cat /etc/hammer/root)
	HMR_V=$($JAVA_PATH -jar $HAMMER_ROOT/hammer.jar --version=simple);
	echo -e "\t\033[33mHammer is installed."
	echo -e "\tVersion: $HMR_V\033[0m"
	LATEST=$(curl -s $REPO/version)
	if [ $LATEST != $HMR_V ]; then
		echo
		echo -e "\033[32mDiscover new release: $LATEST\033[0m"
	fi
else
	echo -e "\t\033[33mHammer is not installed.\033[0m"
fi
echo
echo -e "\t[\033[36m1\033[0m] Install"
echo -e "\t[\033[36m2\033[0m] Uninstall"
echo -e "\t[\033[36m3\033[0m] Update"
echo
read -p "Please input number operation:" OP
if [ -z $OP ]; then
	error "Input is empty."
	exit 5
elif [ $OP == 1 ]; then
        install
elif [ $OP == 2 ]; then
	uninstall
elif [ $OP == 3 ]; then
	update
else
	error "Invalid input."
fi
