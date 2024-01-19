#!/bin/bash
# start-elastic-search-cluster.sh

# Check the current vm.max_map_count value
current_value=$(sysctl -n vm.max_map_count)

# Define the desired value
desired_value=262144

# Compare the current value with the desired value
if [ $current_value -lt $desired_value ]; then
    # Ask the user if they want to raise the value
    echo "The current vm.max_map_count value is $current_value, which is less than $desired_value."
    read -p "Do you want to raise vm.max_map_count to $desired_value? (yes/no) [yes]: " user_input

    # Make 'yes' the default value if the user just presses enter
    user_input=${user_input:-yes}

    # Convert user input to lowercase
    user_input=$(echo $user_input | tr '[:upper:]' '[:lower:]')

    if [[ $user_input == "yes" || $user_input == "y" ]]; then
        # Use sudo to raise the vm.max_map_count value
        echo "Raising vm.max_map_count to $desired_value. Please enter your password if prompted."
        sudo sysctl -w vm.max_map_count=$desired_value
    else
        echo "Continuing without changing vm.max_map_count."
    fi
else
    echo "The current vm.max_map_count value is $current_value, no change needed."
fi

docker-compose -f common.yml -f elastic_cluster.yml up -d
