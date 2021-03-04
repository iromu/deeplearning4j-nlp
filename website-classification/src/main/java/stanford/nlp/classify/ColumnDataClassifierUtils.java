package stanford.nlp.classify;

import com.iromu.dl.nlp.BaseColumnDataClassifierUtils;
import com.iromu.dl.nlp.DatasetNormalizer;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Paths;

import static com.iromu.dl.nlp.DatasetPath.getRootPath;

@Slf4j
public class ColumnDataClassifierUtils {
    public final BaseColumnDataClassifierUtils target;

    public ColumnDataClassifierUtils(DatasetNormalizer normalizer, String modelName) throws IOException, ClassNotFoundException {
        target = new BaseColumnDataClassifierUtils(normalizer, modelName,
                Paths.get(getRootPath(), "datasets", "URL Classification", "parsed"),
                Paths.get(getRootPath(), "datasets", "website-classification-workdir"));
        target.readModel();
    }
}
