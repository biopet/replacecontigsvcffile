# Manual

```bash
java -jar ReplaceContigsVcfFile.jar \
--input input.vcf
--output output.vcf
-R reference.fasta
--contig contignames.tsv
```

A contig.tsv could like this:
```tsv
chr1    1;I;one
chr2    2:II;two
```
Where the alternative names in the second column will be replaced by the one in the second column.
