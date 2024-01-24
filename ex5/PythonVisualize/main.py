import requests
import pandas as pd
import holoviews as hv
from holoviews import opts, dim

hv.extension('bokeh')

# Define the range of ports
port_range = range(8881, 8890)
base_url = "http://localhost:"

# List to hold all URLs
api_endpoints = [f"{base_url}{port}/api/node" for port in port_range]

# List to store the data for chord diagram
chord_data = []

# Fetch data from each endpoint
for url in api_endpoints:
    try:
        response = requests.get(url, timeout=(3, 5))

        if response.status_code == 200:
            node_info = response.json()
            print("Node ID:", node_info[1]["id"])
            for item in node_info[2]:
                target_id = int(item["successor"].split(".")[3].split(":")[0])
                print("Successor: ", target_id)
                chord_data.append({
                    'source': node_info[1]["id"],
                    'target': target_id,
                    'value': 1,
                    'source_description': f'Node {node_info[1]["id"]}',
                    'target_description': f'Node {target_id}'
                })

    except requests.exceptions.Timeout:
        print("Timeout occurred to fetch data from: ", url)
    except requests.exceptions.RequestException as e:
        print("Error occurred while fetching data from:", url, "; Error:", e)

# Create a DataFrame from the collected data
df = pd.DataFrame(chord_data)

df.sort_values(by=['source'], inplace=True, ascending=True)

print(df)

# Generate the chord diagram
chord_diagram = hv.Chord(df)

# Display the diagram with hover tools
chord_diagram.opts(
    opts.Chord(
        cmap='Category20',
        edge_cmap='Category20',
        labels='source_description',
        edge_color=dim('source').str(),
        node_color=dim('index').str(),
        width=1000,
        height=1000,
        tools=['hover']
    )
)

hv.save(chord_diagram, 'chord_diagram.html')
