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
  write_item "Generated log file: $LOG_NAME"
}

function write_found
{
  write_item "${green}Found $1!${clear}"
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



## Configuration and Installation Subroutine START
function configure_and_install_open_mpi
{
  # Configure Open MPI
  write_item "About to configure Open MPI. (This will take a while, please wait!)"
  cd openmpi-4.1.1

  # Configuration command for a real cluster:
  ./configure --prefix=$PREFIX --enable-shared --enable-mpi-thread-multiple --with-verbs --enable-mpirun-prefix-by-default --with-hwloc=$EBROOTHWLOC $SCHEDULER_CONFIGURATION_ARGUMENT --enable-mpi-cxx --with-ucx=$EBROOTUCX

  # Install Open MPI:
  write_item "About to install Open MPI. (This WILL take very long. Please wait!)"
  make install
}
## Configuration and installation subroutine END




# User must provide at least one argument.
if [ "$#" -eq  "0" ]
then
  write "* Please select at least one of the two options:"
  write "  1) -openMpiModule, install a custom Open MPI module localy."
  write "  2) -parallelTools, install Fiji with the parallel macro and OpenMPI Ops plugins."
  exit 1
fi


# Option handling, there are two available option, 1) install custom Open MPI Environment Module 2) install parallel macro and OpenMPI Ops plugins.
OPEN_MPI_MODULE_INSTALLATION=false
PARALLEL_TOOLS_INSTALLATION=false
while test "$#" -gt 0
do
    case "$1" in
        -openMpiModule) write_item "Custom Open MPI Environment Module installation selected."
            OPEN_MPI_MODULE_INSTALLATION=true
            ;;
        -parallelTools) write_item "Fiji and parallel macro and OpenMPI Ops plugins installation selected."
            PARALLEL_TOOLS_INSTALLATION=true
            ;;
        *) write_error "Invalid option!"
            exit 1
            ;;
    esac
    shift
done




# wget must be available:
if ! command -v wget &> /dev/null
then
  write_error "Did not find wget! It must be installed to continue!"
  exit 1
else
  write_found "wget"
fi









if $OPEN_MPI_MODULE_INSTALLATION; then # Start of OPEN_MPI_MODULE_INSTALLATION section. Skip this section if the user did not select this option!



# Set this constant to the correct name of the GCC module on your HPC Cluster:
GCC_COMPILER_MODULE="GCC/10.3.0"
COMPILER_PART=$(echo "$GCC_COMPILER_MODULE" | sed 's;/;;g' )

# Set Open MPI installation directory (prefix):
PREFIX="$HOME"/openmpi-4.1.1-"$COMPILER_PART"/

# Set custom Open MPI module directory:
CUSTOM_MODULE_DIR="$HOME"/Modules/modulefiles/OpenMpi


# The Environment Modules program must exist:
if ! command -v module &> /dev/null
then
  write_error "Environment Modules program does not exist!"
  exit 1
else
  write_found "Environment Modules"
fi

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

write_item "About to load GCC compiler."
# Load the GCC module:
write_item "Will use the following GCC Environment Module: $GCC_COMPILER_MODULE"
module load "$GCC_COMPILER_MODULE"

# Download Open MPI source code, extract archive and remove archive:
FILE=./openmpi-4.1.1.tar.gz
if [ -f "$FILE" ]
then
  write_item "Open MPI has already been downloaded!"
else
  write_item "Downloading Open MPI. (This might take a while, please wait)"
  wget https://download.open-mpi.org/release/open-mpi/v4.1/openmpi-4.1.1.tar.gz
fi

write_item "Extracting Open MPI archive!"
tar xvfz openmpi-4.1.1.tar.gz
##rm -r openmpi-4.1.1.tar.gz

# Scheduler directory must exist.
if [ -d "$DIR" ]; then
  write_item "Scheduler directory $DIR found!"
  configure_and_install_open_mpi
else
  write_error "Scheduler directory $DIR must exist! Try running this script in an interactive job."
  exit 1
fi

# OpenFabrics error fix:
echo 'btl_openib_allow_ib = true' >> "$PREFIX"/etc/openmpi-mca-params.conf




write_item "About to create custom Open MPI Environment Module."
# Create custom module:
mkdir -p "$CUSTOM_MODULE_DIR"

# Create the module file:
echo "$MODULE_TEXT" > "$CUSTOM_MODULE_DIR"/4.1.1-"$COMPILER_PART"

# Automatically load custom module:
echo "module use --append $HOME/Modules/modulefiles" >> "$HOME"/.bashrc


write_item "The custom Environment Module should appear in the list bellow:"
# Display available Open MPI modules:
module avail openmpi > /dev/tty

cd ..
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
  write_error "Did not find Java Developement Kit 8! It must be installed to continue!"
  exit 1
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
  write_warning "Did not find Maven! I will install it localy!"

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
else
  write_found "Maven"
fi

# Download Fiji
FILE=./fiji-linux64.zip
if [ -f "$FILE" ]
then
  write_item "Fiji has already been downloaded!"
else
  write_item "Downloading Fiji (this will take a while please wait)!"
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
write_item "Generated log file: $LOG_NAME"


fi # End of PARALLEL TOOLS INSTALLATION section!
