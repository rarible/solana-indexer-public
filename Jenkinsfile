@Library('shared-library') _

def pipelineConfig = [
    "services": [
        [name: 'solana-migration', path: './solana-migration'],
        [name: 'solana-indexer', path: './solana-indexer'],
        [name: 'solana-api', path: './solana-api']
    ],
    "slackChannel": "#protocol-duty"
]

pipelineAppCI(pipelineConfig)
