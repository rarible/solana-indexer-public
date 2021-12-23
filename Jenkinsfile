@Library('shared-library@v20211117-1') _

def pipelineConfig = [
    "stackName": "protocol-solana",
    "services": [
        [name: 'solana-block-scanner', path: './solana-block-scanner'],
        [name: 'solana-indexer', path: './solana-indexer']
    ],
    "slackChannel": "#protocol-duty"
]

serviceCI(pipelineConfig)
