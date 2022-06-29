#!/bin/bash
# Save output to log file:
TIME=$(date +%s)
LOG_NAME="fiji-hpc-helper-$TIME.log"
exec &>> "$LOG_NAME"

# If any one step fails the script should stop:
set -e
set -o pipefail




# Echo formating helper subroutines START:
function write
{
  echo -e "$1"> /dev/tty
}

function write_item
{
  write "* $1"
}

function write_warning
{
  write_item "${yellow}WARNING:${clear} $1"
}

function write_error
{
  write_item "${red}ERROR:${clear} $1"
  write_item "Generated log file: ${yellow}$LOG_NAME${clear}"
}

function write_found
{
  write_item "${green}Found $1!${clear}"
}

function write_note
{
  write_item "${blue}Note:${clear} $1"
}

function break_line
{
  write "\n"
}


# Color variables
red='\033[0;31m'
green='\033[0;32m'
yellow='\033[0;33m'
blue='\033[0;34m'
magenta='\033[0;35m'
cyan='\033[0;36m'
# Clear the color after that
clear='\033[0m'
# Echo formatting helper subroutines END.




# This function will attempt to locate the most recent version
# of a module that matches a key approximatly,
# use lower case for key.
function find_module
{
  # The serach key provided by the user of the subroutine:
  SEARCH_KEY="$1"

  # Find a list of modules:
  MODULES=$((module avail $SEARCH_KEY) |& awk -v key="$SEARCH_KEY" 'BEGIN{IGNORECASE=1}($1~/'"$key"'.*/){for(i=1;i<=NF;++i)if($i~/^'"$1"'\/.*/)print $i}');

  # If a version is provided, filter all results with it:
  if [ $# -eq 2 ]
  then
    VERSION=$2
    MODULES=$(echo $MODULES | grep $VERSION)
  fi

  # If  nothing is found this subroutine should return false:
  if [ -z "$MODULES" ]
  then
    echo false
    return
  fi

  # Reverse list to get most recent version of the module:
  list=$( echo "$MODULES" | tac );

  # Get first item of list (most recent version):
  array=( $list )
  echo ${array[0]}
  return
}




## Configuration and Installation Subroutine START
function configure_and_install_open_mpi
{
  # memorize the current working folder
  pushd .

  # Configure Open MPI
  write_item "About to configure Open MPI. (This will take a while, please wait.)"
  cd openmpi-${OPENMPI_VERSION}

  # Configuration command for a real cluster:
  ./configure --prefix=$PREFIX --enable-shared --enable-mpi-thread-multiple --with-verbs --enable-mpirun-prefix-by-default --with-hwloc=$EBROOTHWLOC $SCHEDULER_CONFIGURATION_ARGUMENT --enable-mpi-cxx --with-ucx=$EBROOTUCX

  # Install Open MPI:
  write_item "About to install Open MPI. (This WILL take very long, please wait.)"
  make install

  # return to the memorized/previous working folder
  popd
}
## Configuration and installation subroutine END




# User must provide at least one argument.
if [ "$#" -eq  "0" -o "$#" -gt 2 ]
then
  write "* Please select at least one of the two options:"
  write "  1) -openMpiModule, install a custom Open MPI module localy."
  write "  2) -parallelTools, install Fiji with the parallel macro and OpenMPI Ops plugins."
  write "  3) -installersRemoval, remove files used during each one of the two above options."
  break_line
  write_note "The script will operate (create folders) in the current working directory (that means ${yellow}$PWD${clear})"
  write_note "With the option (1) it will add files into folder: ${yellow}$HOME/Modules/modulefiles${clear}"
  break_line

  exit 1
fi

write_note "More details can be monitored in the currently populated log file ${yellow}${LOG_NAME}${clear}"


# Option handling, there are two available option, 1) install custom Open MPI Environment Module 2) install parallel macro and OpenMPI Ops plugins.
OPEN_MPI_MODULE_INSTALLATION=false
PARALLEL_TOOLS_INSTALLATION=false
INSTALLERS_REMOVAL=false
while test "$#" -gt 0
do
    case "$1" in
        -openMpiModule) write_item "Custom Open MPI Environment Module installation selected."
            OPEN_MPI_MODULE_INSTALLATION=true
            ;;
        -parallelTools) write_item "Fiji and parallel macro and OpenMPI Ops plugins installation selected."
            PARALLEL_TOOLS_INSTALLATION=true
            ;;
        -installersRemoval) write_item "Removal of installers selected."
            INSTALLERS_REMOVAL=true
            ;;
        *) write_error "Invalid option!"
            exit 1
            ;;
    esac
    shift
done



# Start of INSTALLERS_REMOVAL section. Skip this section if the user did not select this option!
function delete_item
{
  ITEM=$1
  if [ -f $ITEM  ] 
  then
    write_item "About to delete file $ITEM."
    rm $ITEM
    write_item "Item $ITEM deleted!"
  elif [ -d $ITEM  ]
  then
    write_item "About to delete directory  $ITEM."
    rm -rf $ITEM
    write_item "Directory $ITEM deleted!"
  fi
}


if $INSTALLERS_REMOVAL; then
  write_item "Removing installers!"
  delete_item apache-maven-3.8.6
  delete_item apache-maven-3.8.6-bin.zip
  delete_item fiji-linux64.zip
  delete_item openmpi-4.1.1
  delete_item openmpi-4.1.1.tar.gz
  delete_item parallel-macro
  delete_item scijava-parallel-mpi
  exit 1
fi
# End of INSTALLERS_REMOVAL section.



# wget must be available:
if ! command -v wget &> /dev/null
then
  write_error "Did not find wget! It must be installed to continue!"
  exit 1
else
  write_found "wget"
fi









if $OPEN_MPI_MODULE_INSTALLATION; then # Start of OPEN_MPI_MODULE_INSTALLATION section. Skip this section if the user did not select this option!

# The Environment Modules program must exist:
if ! command -v module &> /dev/null
then
  write_error "Environment Modules program does not exist!"
  exit 1
else
  write_found "Environment Modules"
fi

# GCC compiler Environment Module must exist:
GCC_COMPILER_MODULE=$(find_module gcc)
if [ $GCC_COMPILER_MODULE != false ]
then
  module load $GCC_COMPILER_MODULE
  write_found "GCC Environment Module"
else
  write_error "Could not find a GCC Environment Module!"
  exit 1
fi

write_item "Will use the following GCC Environment Module: $GCC_COMPILER_MODULE"
COMPILER_PART=$(echo "$GCC_COMPILER_MODULE" | sed 's;/;;g' )


# Set custom Open MPI version
OPENMPI_VERSION="4.1.1"

# Set custom Open MPI module directory:
CUSTOM_MODULES_ROOT="$HOME"/Modules/modulefiles
CUSTOM_MODULE_DIR="$HOME"/Modules/modulefiles/OpenMpi
CUSTOM_MODULE_NAME="${OPENMPI_VERSION}-${COMPILER_PART}-CustomModule"

# Set Open MPI installation directory (prefix):
PREFIX="$PWD/openmpi-$CUSTOM_MODULE_NAME"


# Contents of the Environment Module file:
MODULE_TEXT="#%Module 1.0
#
#  Open MPI module for use with 'environment-modules' package:
#
conflict mpi
prepend-path PATH \"$PREFIX/bin\"
prepend-path LD_LIBRARY_PATH \"$PREFIX/lib\"
module load \"$GCC_COMPILER_MODULE\" "

# Set the scheduler:
# Note that at least one scheduler must exist:
SCHEDULER_CONFIGURATION_ARGUMENT="NONE"
if command -v qsub --version &> /dev/null
then
  write_found "OpenPBS or PBS Pro"
  DIR="/opt/pbs"
  SCHEDULER_CONFIGURATION_ARGUMENT="--with-tm=$DIR"
elif command -v sinfo -V &> /dev/null
then
  write_found "Slurm Workload Manager"
  DIR="/usr"
  SCHEDULER_CONFIGURATION_ARGUMENT="--with-slurm=$DIR" # ToDO: Make sure this is correct!
elif command -v lsid &> /dev/null
then
  write_found "IBM Spectrum LSF"
  DIR="/usr/share/lsf"
  SCHEDULER_CONFIGURATION_ARGUMENT="--with-lsf=$DIR" # ToDo: Think about this!
else
  write_error "No supported scheduler was found!"
  write "  Supported schedulers are 1) OpenPBS or PBS Pro, 2) Slurm Workload Manager and 3) IBM Spectrum LSF."
  exit 1
fi

write_item "Will use $SCHEDULER_CONFIGURATION_ARGUMENT option in Open MPI configuration."

# Download Open MPI source code, extract archive and remove archive:
FILE=./openmpi-${OPENMPI_VERSION}.tar.gz
if [ -f "$FILE" ]
then
  write_item "Open MPI has already been downloaded!"
else
  write_item "Downloading Open MPI. (This might take a while, please wait.)"
  wget https://download.open-mpi.org/release/open-mpi/v${OPENMPI_VERSION:0:3}/openmpi-${OPENMPI_VERSION}.tar.gz
fi

write_item "Extracting Open MPI archive!"
tar xvfz openmpi-${OPENMPI_VERSION}.tar.gz
##rm -r openmpi-${OPENMPI_VERSION}.tar.gz

# Scheduler directory must exist.
if [ -d "$DIR" ] && [ -r "$DIR" ]
then
  write_item "Scheduler directory $DIR found!"
  configure_and_install_open_mpi
else
  write_error "Scheduler directory $DIR must exist and be accessible!"
  write_item "Try running this script in an interactive job. In PBS for example run: qsub -q qexp -l select=1 -I"
  exit 1
fi

# OpenFabrics error fix:
echo 'btl_openib_allow_ib = true' >> "$PREFIX"/etc/openmpi-mca-params.conf




write_item "About to create custom Open MPI Environment Module: $CUSTOM_MODULE_DIR"
# Create custom module:
mkdir -vp "$CUSTOM_MODULE_DIR"

# Create and enable the module file:
echo "$MODULE_TEXT" > "$CUSTOM_MODULE_DIR/$CUSTOM_MODULE_NAME"
module use --append $CUSTOM_MODULES_ROOT

# Make sure to automatically load custom module (if not already there):
grep -q "$CUSTOM_MODULES_ROOT" "$HOME"/.bashrc || {
  echo "module use --append $CUSTOM_MODULES_ROOT" >> "$HOME"/.bashrc;
  write_item "Note: Adding a line \"module use --append $CUSTOM_MODULES_ROOT\" into ${HOME}/.bashrc file.";
  }


write_item "The custom Environment Module >> OpenMpi/$CUSTOM_MODULE_NAME << should appear in the list bellow:"
# Display available Open MPI modules:
module avail openmpi 2> /dev/tty
write_item "Installation of Custom Open MPI Environment Module finished."

write_item "${blue}The new module can be later loaded with the command:${clear}"
write_item "${blue}module load OpenMpi/$CUSTOM_MODULE_NAME${clear}"

fi # End OPEN_MPI_MODULE_INSTALLATION section.










if $PARALLEL_TOOLS_INSTALLATION; then # Start of PARALLEL_TOOLS_INSTALLATION section. Skip this section is the user has not selected it!

# git must be available:
if ! command -v git &> /dev/null
then
  write_error "Did not find Git! It must be installed to continue!"
  exit 1
else
  write_found "Git"
fi

# Java (JDK) 8 must be available:
if ! command -v javac &> /dev/null
then
  write_warning "Did not find Java Developement Kit 8! I will try to find and load an Environment Module!"
  JAVA_MODULE=$( find_module java "1.8" )
  if [ $JAVA_MODULE != false ]
  then
    module load $JAVA_MODULE
    write_found "Java 8 Environment Module: $JAVA_MODULE"
  else
    write_error "Did not find a Java Development Kit version 8 Environment Module."
    exit 1
  fi
else
  version=$("java" -version 2>&1 | awk -F '"' '/version/ {print $2}')
  echo $version
  if [[ "$version" < "1.8" ]]
  then
    write_error "Did not find Java Developement Kit version 8! Fiji is compatible only with version 8."
    exit 1
  fi
  write_found "Java 8"
fi


# Maven must be available:
if ! command -v mvn &> /dev/null
then
  write_warning "Did not find Maven! I try to find and load a module!"
  MAVEN_MODULE=$(find_module maven)
  if [ $MAVEN_MODULE != false ]
  then
    module load $MAVEN_MODULE
  else
    write_warning "Did not find a Maven Module! I will install it localy!"
  
    # Download Maven:
    FILE=./apache-maven-3.8.6-bin.zip
    if [ -f "$FILE" ]
    then
      write_item "Maven has already been downloaded!"
    else
      write_item "Downloading maven!"
      wget https://dlcdn.apache.org/maven/maven-3/3.8.6/binaries/apache-maven-3.8.6-bin.zip
    fi
  
    # Install Maven:
    unzip -o apache-maven-3.8.6-bin.zip
    ##rm apache-maven-3.8.6-bin.zip
    cd apache-maven-3.8.6
    pwd=$(pwd)
    export PATH="$pwd/bin:$PATH"
    cd ..
    write_item "Maven installed!"
  fi
else
  write_found "Maven"
fi

# Download Fiji
FILE=./fiji-linux64.zip
if [ -f "$FILE" ]
then
  write_item "Fiji has already been downloaded!"
else
  write_item "Downloading Fiji. (This will take a while, please wait.)"
  wget https://downloads.imagej.net/fiji/latest/fiji-linux64.zip
fi

write_item "About to install Fiji!"
# Install Fiji
unzip -o fiji-linux64.zip
##rm fiji-linux64.zip
cd Fiji.app
FIJI_DIR=$(pwd)
cd ..
write_item "Fiji installed!"

# Install Parallel Macro
DIR=./parallel-macro
if [ -d "$DIR" ]
then
  write_warning "I will use existing OLD parallel macro directory!"
else
  write_item "Cloning parallel macro localy!"
  git clone https://github.com/fiji-hpc/parallel-macro.git
fi
cd parallel-macro
bash build.sh "$FIJI_DIR"
cd ..
write_item "Parallel macro plugin installed!"

# Install OpenMPI Ops
DIR=./scijava-parallel-mpi
if [ -d "$DIR" ]
then
  write_warning "I will use existing OLD OpenMPI Ops directory!"
else
  write_item "Cloning OpenMPI Ops localy!"
  git clone https://github.com/fiji-hpc/scijava-parallel-mpi
fi
cd scijava-parallel-mpi
bash build.sh "$FIJI_DIR"
cd ..
write_item "OpenMPI Ops plugin installed!"

# Inspect installation (the jar files of parallel macro and OpenMPI Ops should be in jars of Fiji):
INSTALLATION_RESULT="${red}UNSUCCESSFULLY${clear}"
if [ -d "$FIJI_DIR/jars" ]
then
  PARALLEL_MACRO_PATH="$FIJI_DIR/jars/ParallelMacro-0.5.0-SNAPSHOT.jar"
  OPENMPI_OPS_PATH="$FIJI_DIR/jars/scijava-parallel-mpi-1.0-SNAPSHOT.jar"
  if [ -f "$PARALLEL_MACRO_PATH" ] && [ -f "$OPENMPI_OPS_PATH" ]
  then
    INSTALLATION_RESULT="${green}SUCCESSFULLY${clear}"
  fi
fi

write_item "Installation of Fiji with the parallel macro and OpenMPI Ops plugins finished $INSTALLATION_RESULT!"

fi # End of PARALLEL TOOLS INSTALLATION section!


write_item "Generated log file: ${yellow}$LOG_NAME${clear}"
